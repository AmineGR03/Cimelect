# Identifiants de démonstration

Ces identifiants sont destinés au développement local uniquement. Ils ne doivent pas être conservés tels quels en production.

## Compte administrateur bootstrap

| Rôle | Email | Mot de passe |
| --- | --- | --- |
| Administrateur | `admin@cimelect.local` | `Admin123!` |

Ce compte est créé automatiquement par `backend/src/main/java/com/cimelect/config/DataInitializer.java` lorsque la table `users` est vide. Les valeurs proviennent de `backend/src/main/resources/application.properties`.

## Utilisation

1. Démarrer PostgreSQL et créer la base `cimelect_db`.
2. Démarrer le backend sur `http://localhost:8080`.
3. Démarrer le frontend sur `http://localhost:3000`.
4. Se connecter avec le compte administrateur ci-dessus.

Le frontend stocke le token JWT reçu après connexion dans le stockage local du navigateur et l’envoie automatiquement dans l’en-tête `Authorization: Bearer <token>`.

## Autres rôles

Les comptes de démonstration `RESPONSABLE` et `AGENT_IMPORT_EXPORT` sont également créés automatiquement par `DataInitializer` :

| Rôle | Email | Mot de passe |
| --- | --- | --- |
| Responsable | `responsable@cimelect.local` | `Responsable123!` |
| Agent import/export | `agent@cimelect.local` | `Agent123!` |

L’administrateur peut aussi créer d’autres comptes depuis l’endpoint backend `POST /api/users` avec un corps similaire :

```json
{
  "firstName": "Agent",
  "lastName": "Demo",
  "email": "agent@cimelect.local",
  "password": "Agent123!",
  "role": "AGENT_IMPORT_EXPORT",
  "enabled": true
}
```

Rôles acceptés : `ADMINISTRATEUR`, `RESPONSABLE`, `AGENT_IMPORT_EXPORT`.

## Sécurité

Changez le mot de passe bootstrap, le secret JWT et les identifiants PostgreSQL avant un déploiement. Ce fichier contient des accès de démonstration et ne doit pas être utilisé comme coffre-fort de secrets.
