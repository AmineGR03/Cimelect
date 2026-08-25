# Cimelect

Cimelect est une application de pilotage des opérations de commerce international. Elle centralise les imports, les exports, les expéditions, les partenaires, les documents et les indicateurs de performance dans une interface web sécurisée.

## Architecture

- `backend/` : API Spring Boot, Spring Security JWT, JPA/Hibernate et PostgreSQL.
- `frontend/` : application React avec Redux Toolkit, React Router et Bootstrap.
- `backend/src/main/resources/application.properties` : configuration de la base, du port, du JWT et du compte bootstrap.

## Fonctionnalités

- Authentification et déconnexion par JWT.
- Tableau de bord avec KPI : opérations actives, expéditions suivies, délai moyen et taux d’anomalie.
- Visualisation de la tendance des imports et exports.
- Suivi des alertes IA associées aux expéditions.
- Consultation des opérations import/export et de leur statut.
- Suivi des expéditions en cours.
- Consultation des clients et fournisseurs.
- Gestion des documents et des exigences documentaires via l’API.
- Gestion des utilisateurs selon le rôle connecté.

## Prérequis

- Java 17 ou version compatible avec le projet Maven.
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

## Rôles

- `ADMINISTRATEUR` : accès complet, dashboard, utilisateurs, audit et paramétrage.
- `RESPONSABLE` : dashboard, suivi des opérations, expéditions et audit selon les droits backend.
- `AGENT_IMPORT_EXPORT` : opérations, expéditions, clients, fournisseurs et produits selon les droits backend.

Les permissions sont contrôlées côté backend par Spring Security. Le masquage des liens dans le frontend améliore l’expérience, mais ne remplace pas cette protection.

## Configuration PostgreSQL

Les valeurs de développement actuelles sont définies dans `backend/src/main/resources/application.properties` :

- hôte : `localhost`
- port : `5432`
- base : `cimelect_db`
- utilisateur : `postgres`

Adaptez le mot de passe et les paramètres d’environnement avant toute utilisation hors développement local.
