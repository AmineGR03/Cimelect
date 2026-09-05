# Cimelect

Cimelect est une application de pilotage des opérations de commerce international. Elle centralise les imports, les exports, les expéditions, les partenaires, les documents et les indicateurs de performance dans une interface web sécurisée.

## Architecture

- `backend/` : API Spring Boot, Spring Security JWT, JPA/Hibernate et PostgreSQL.
- `frontend/` : application React avec Redux Toolkit, React Router et Bootstrap.
- `backend/src/main/resources/application.properties` : configuration de la base, du port, du JWT et du compte bootstrap.

## Fonctionnalités

### Authentification et Sécurité
- Authentification sécurisée par JWT.
- Déconnexion et gestion de session.
- Contrôle d'accès basé sur les rôles (RBAC).
- Protection des routes selon les permissions utilisateur.

### Tableau de Bord (Dashboard)
- Vue d'ensemble des opérations actives.
- KPI en temps réel : opérations actives, expéditions suivies, délai moyen, taux d'anomalie.
- Graphiques de tendance des imports et exports.
- Suivi des alertes IA associées aux expéditions.
- Carte de statut avec badges visuels.

### Gestion des Opérations
- **Opérations** : consultation et suivi des opérations import/export.
- **Gestion des Opérations** : création, modification et gestion complète des opérations.
- Statuts et historique des opérations.
- Filtrage, recherche, tri et pagination.

### Gestion des Expéditions
- **Expéditions** : suivi et consultation de toutes les expéditions.
- **Gestion des Expéditions** : gestion complète incluant les expéditions livrées.
- Alertes et suivi des anomalies.
- Historique des mouvements des expéditions.

### Gestion des Documents
- Consultation des documents par opération.
- Dépôt et téléchargement de fichiers.
- Suppression de documents.
- Contrôle des exigences documentaires (import/export).
- Validation et archivage des documents.

### Gestion des Partenaires
- Consultation et management des clients, fournisseurs et produits.
- **Fiche Partenaire** : détails complets avec opérations et indicateurs associés.
- Historique des transactions par partenaire.

### Gestion des Utilisateurs
- **Utilisateurs** : gestion complète des comptes utilisateur (admin/responsable).
- Création, modification et suppression d'utilisateurs.
- Gestion des rôles et permissions.
- Visibilité selon les droits de l'utilisateur connecté.

### Audit et Conformité
- **Journal d'Audit** : enregistrement complet de toutes les opérations.
- Historique détaillé avec timestamp et auteur.
- Traçabilité des modifications.
- Rapport d'audit pour conformité.

### Profil Utilisateur
- **Profil** : consultation et modification des informations personnelles.
- Gestion des paramètres de compte.
- Historique personnel.

### Expérience Utilisateur
- Recherche, filtres, tri et pagination sur l'ensemble des listes.
- États de chargement visibles (spinners).
- Notifications de succès et d'erreur.
- Interface responsive et intuitive.
- Navigation cohérente selon les rôles.

## Modules et Navigation

L'application est structurée en modules accessibles selon les droits de l'utilisateur :

| Module | Accès | Description |
|--------|-------|-------------|
| **Dashboard** | ADMINISTRATEUR, RESPONSABLE | Vue d'ensemble avec KPI et indicateurs de performance |
| **Opérations** | AGENT_IMPORT_EXPORT, RESPONSABLE | Consultation des opérations import/export |
| **Gestion Opérations** | RESPONSABLE | Gestion complète et création d'opérations |
| **Expéditions** | AGENT_IMPORT_EXPORT, RESPONSABLE | Suivi et consultation des expéditions |
| **Gestion Expéditions** | RESPONSABLE | Gestion complète des expéditions |
| **Documents** | AGENT_IMPORT_EXPORT, RESPONSABLE | Gestion documentaire par opération |
| **Partenaires** | AGENT_IMPORT_EXPORT | Clients, fournisseurs et produits |
| **Fiche Partenaire** | AGENT_IMPORT_EXPORT | Détails et indicateurs des partenaires |
| **Utilisateurs** | ADMINISTRATEUR, RESPONSABLE | Gestion des comptes et permissions |
| **Audit** | ADMINISTRATEUR, RESPONSABLE | Journal complet d'audit et traçabilité |
| **Profil** | TOUS | Gestion du compte personnel |

## Prérequis

- JDK 21.
- Node.js et npm.
- PostgreSQL lancé localement.
- Une base PostgreSQL nommée `cimelect_db`.

## Démarrage du backend

Depuis la racine :

```powershell
cd backend
./mvnw.cmd spring-boot:run
```

L’API est disponible sur `http://localhost:8080`.

Au démarrage, `DataInitializer` crée automatiquement :

- le compte administrateur bootstrap s’il n’existe aucun utilisateur ;
- les comptes de démonstration responsable et agent import/export ;
- les exigences documentaires par défaut pour les imports et les exports.

Les informations du compte sont dans [CREDENTIALS.md](CREDENTIALS.md).

## Démarrage du frontend

Dans un autre terminal :

```powershell
cd frontend
npm install
npm start
```

L’interface est disponible sur `http://localhost:3000`.

Par défaut, le frontend appelle `http://localhost:8080/api`. Pour utiliser une autre URL :

```powershell
$env:REACT_APP_API_URL="http://localhost:8080/api"
npm start
```

## Vérifications

```powershell
cd frontend
npm test -- --watchAll=false
npm run build
```

Les tests frontend couvrent notamment l’authentification, les rôles, les opérations,
les documents, les expéditions et les utilisateurs. Les tests backend nécessitent
PostgreSQL actif sur `localhost:5432`.

## Rôles

### ADMINISTRATEUR
- Accès complet à tous les modules de l'application.
- Gestion complète des utilisateurs (création, modification, suppression).
- Accès au dashboard avec KPI et indicateurs globaux.
- Consultation du journal d'audit complet.
- Gestion des rôles et permissions.
- Paramétrage système et configuration.

### RESPONSABLE
- Accès au dashboard avec vue d'ensemble des opérations.
- Suivi complet des opérations (import/export).
- Gestion des opérations : création, modification, archivage.
- Suivi et gestion complète des expéditions.
- Accès aux documents et vérification des exigences.
- Consultation du journal d'audit.
- Gestion limitée des utilisateurs (selon configuration backend).

### AGENT_IMPORT_EXPORT
- Consultation des opérations import/export en cours.
- Suivi des expéditions associées.
- Gestion documentaire complète (dépôt, téléchargement, suppression).
- Accès aux partenaires : clients, fournisseurs et produits.
- Consultation des fiches partenaire avec historique.
- Pas accès au dashboard, audit ou gestion utilisateurs.

### Sécurité des rôles
- Les permissions sont contrôlées côté **backend par Spring Security** (sécurité primaire).
- Le frontend masque les liens selon le rôle connecté (amélioration UX, pas de sécurité).
- Chaque requête API vérifie les droits de l'utilisateur.
- Impossible de contourner les restrictions en modifiant le frontend.

## État et améliorations restantes

La checklist détaillée se trouve dans [AUDIT_CDC_TODO.md](AUDIT_CDC_TODO.md). Les principaux travaux restants sont :

- tester les trois rôles sur un environnement complet ;
- renforcer les tests d’autorisation et d’intégration ;
- configurer Java 21 automatiquement pour Maven et les environnements de déploiement ;
- ajouter observabilité, logs structurés, sauvegardes et documentation API ;
- améliorer accessibilité, pagination côté serveur et gestion des erreurs réseau ;
- traiter ultérieurement la fonctionnalité IA, volontairement hors périmètre actuel.

## Configuration PostgreSQL

Les valeurs de développement actuelles sont définies dans `backend/src/main/resources/application.properties` :

- hôte : `localhost`
- port : `5432`
- base : `cimelect_db`
- utilisateur : `postgres`

Adaptez le mot de passe et les paramètres d’environnement avant toute utilisation hors développement local.
