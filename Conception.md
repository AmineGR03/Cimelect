# Conception OOP et UML

Cette documentation sert de base à la conception objet de Cimelect. Les diagrammes décrivent
la fonctionnalité importante de **gestion documentaire d'une opération** : consulter,
télécharger et déposer les documents liés à une opération. L'authentification,
l'autorisation et le JWT sont volontairement exclus des scénarios ci-dessous afin de
se concentrer sur le fonctionnement métier.

## Principes OOP à appliquer

- **Encapsulation** : les attributs des entités restent privés et sont manipulés par
  leurs méthodes ou par les services métier ; les règles ne doivent pas être placées
  dans les contrôleurs.
- **Abstraction** : les contrôleurs exposent des DTO (`DocumentResponse`) et ne
  révèlent ni les entités JPA ni le chemin physique des fichiers.
- **Héritage et polymorphisme** : les types de documents et d'opérations sont des
  énumérations extensibles (`DocumentType`, `OperationType`) ; le comportement varie
  selon le type sans dupliquer le contrôleur.
- **Responsabilité unique** : le contrôleur reçoit la requête, le service applique
  les règles, le repository persiste, le mapper transforme et le service d'audit
  trace l'action.
- **Composition** : une `Operation` possède des `Document`, des `Shipment` et des
  `OperationLine`. La suppression d'une opération doit respecter le cycle de vie de
  ses éléments associés.
- **Dépendances inversées** : `DocumentService` dépend d'abstractions d'accès aux
  données (`DocumentRepository`, `OperationRepository`) et non d'une implémentation
  SQL directe.

## Classes principales et responsabilités

| Classe | Type | Responsabilité |
|--------|------|----------------|
| `DocumentController` | Contrôleur | Expose les endpoints de consultation, dépôt, téléchargement et suppression. |
| `DocumentService` | Service métier | Vérifie le fichier et l'état de l'opération, stocke le fichier, persiste le document et déclenche l'audit. |
| `Document` | Entité | Représente un fichier lié à une opération : type, nom, taille, contenu et date de dépôt. |
| `Operation` | Entité | Porte la référence, le type, le statut et les documents associés ; une opération clôturée ne reçoit plus de document. |
| `RequiredDocument` | Entité métier | Définit les documents obligatoires selon le type d'opération. |
| `DocumentRepository` | Repository | Recherche et persiste les documents, notamment par identifiant d'opération. |
| `OperationRepository` | Repository | Recherche une opération existante et non supprimée. |
| `DocumentMapper` | Mapper | Transforme une entité `Document` en DTO de réponse. |
| `AuditService` | Service transverse | Enregistre les dépôts et suppressions dans le journal d'audit. |

## Diagramme de classes

```mermaid
classDiagram
    class DocumentController {
        +byOperation(operationId) List~DocumentResponse~
        +upload(operationId, type, file) DocumentResponse
        +download(id) Resource
        +delete(id, confirmed) void
    }
    class DocumentService {
        +findByOperation(operationId) List~DocumentResponse~
        +upload(operationId, type, file) DocumentResponse
        +download(id) Resource
        +delete(id, confirmed) void
        -store(file) Path
    }
    class Operation {
        +Long id
        +String reference
        +OperationType type
        +OperationStatus status
        +isClosed() boolean
    }
    class Document {
        +Long id
        +DocumentType type
        +String originalFilename
        +String storagePath
        +String contentType
        +Long fileSize
        +Instant uploadedAt
    }
    class RequiredDocument {
        +Long id
        +OperationType operationType
        +DocumentType documentType
        +boolean required
    }
    class DocumentRepository {
        <<interface>>
        +findByOperationId(operationId)
        +save(document)
    }
    class OperationRepository {
        <<interface>>
        +findByIdAndDeletedFalse(id)
    }
    class DocumentMapper {
        +toResponse(document) DocumentResponse
    }
    class AuditService {
        +log(user, entity, id, action, message)
    }

    DocumentController --> DocumentService : appelle
    DocumentService --> DocumentRepository : persiste
    DocumentService --> OperationRepository : vérifie l'opération
    DocumentService --> DocumentMapper : transforme
    DocumentService --> AuditService : trace
    Operation "1" o-- "0..*" Document : contient
    Operation "1" --> "0..*" RequiredDocument : est soumis à
```

## Diagramme de cas d'utilisation

```mermaid
flowchart LR
    agent[Agent import/export]
    responsable[Responsable]
    system((Système Cimelect))

    subgraph Gestion documentaire
        consulter[Consulter les documents d'une opération]
        deposer[Déposer un document]
        telecharger[Télécharger un document]
        supprimer[Supprimer un document avec confirmation]
        exigences[Consulter les exigences documentaires]
    end

    agent --> consulter
    agent --> deposer
    agent --> telecharger
    agent --> supprimer
    agent --> exigences
    responsable --> consulter
    responsable --> telecharger
    responsable --> exigences
    system --> consulter
    system --> deposer
    system --> telecharger
    system --> supprimer
```

> Les acteurs sont représentés comme déjà identifiés. Les étapes de connexion,
> de validation du JWT et de contrôle des rôles ne font pas partie de ce périmètre.

## Séquence 1 : consulter et télécharger un document

```mermaid
sequenceDiagram
    actor Utilisateur
    participant Frontend
    participant Controller as DocumentController
    participant Service as DocumentService
    participant DB as DocumentRepository
    participant Stockage as Systeme de fichiers

    Utilisateur->>Frontend: Ouvre les documents d'une opération
    Frontend->>Controller: GET /operations/{operationId}/documents
    Controller->>Service: findByOperation(operationId)
    Service->>DB: findByOperationId(operationId)
    DB-->>Service: Liste des Document
    Service-->>Controller: Liste de DocumentResponse
    Controller-->>Frontend: 200 OK + documents
    Frontend-->>Utilisateur: Affiche les documents

    Utilisateur->>Frontend: Clique sur Telecharger
    Frontend->>Controller: GET /documents/{id}/download
    Controller->>Service: get(id)
    Service->>DB: findById(id)
    DB-->>Service: Document
    Controller->>Service: download(id)
    Service->>Stockage: Lit storagePath
    Stockage-->>Service: Resource du fichier
    Service-->>Controller: Resource
    Controller-->>Frontend: 200 OK + fichier
    Frontend-->>Utilisateur: Lance le téléchargement
```

## Séquence 2 : déposer un document

```mermaid
sequenceDiagram
    actor Agent
    participant Frontend
    participant Controller as DocumentController
    participant Service as DocumentService
    participant OpDB as OperationRepository
    participant Stockage as Systeme de fichiers
    participant DocDB as DocumentRepository
    participant Audit as AuditService

    Agent->>Frontend: Sélectionne un fichier et un type
    Frontend->>Controller: POST /operations/{operationId}/documents
    Controller->>Service: upload(operationId, type, file)
    Service->>Service: Vérifie fichier non vide
    Service->>OpDB: findByIdAndDeletedFalse(operationId)
    OpDB-->>Service: Operation
    Service->>Service: Vérifie que l'opération n'est pas clôturée
    Service->>Stockage: store(file)
    Stockage-->>Service: storagePath
    Service->>DocDB: save(Document)
    DocDB-->>Service: Document persisté
    Service->>Audit: log(DOCUMENT_UPLOAD)
    Audit-->>Service: Trace enregistrée
    Service-->>Controller: DocumentResponse
    Controller-->>Frontend: 201 Created
    Frontend-->>Agent: Confirme le dépôt

    alt Fichier absent ou vide
        Service-->>Controller: BusinessException
        Controller-->>Frontend: 400 Bad Request
    else Opération clôturée ou introuvable
        Service-->>Controller: BusinessException ou ResourceNotFoundException
        Controller-->>Frontend: Erreur métier
    end
```
