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
- Suivi et gestion de toutes les expéditions, y compris les expéditions livrées.
- Consultation et gestion des clients, fournisseurs et produits.
- Gestion des documents par opération : dépôt, téléchargement, suppression et contrôle des exigences.
- Historique des opérations et journal d’audit.
- Fiche partenaire avec opérations et indicateurs associés.
- Profil utilisateur et modification des informations personnelles.
- Recherche, filtres, tri et pagination sur les listes principales.
- Gestion des utilisateurs selon le rôle connecté.
- Interface avec états de chargement, succès, erreur, focus et sélection visibles.

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

- `ADMINISTRATEUR` : accès complet, dashboard, utilisateurs, audit et paramétrage.
- `RESPONSABLE` : dashboard, suivi des opérations, expéditions et audit selon les droits backend.
- `AGENT_IMPORT_EXPORT` : opérations, expéditions, clients, fournisseurs et produits selon les droits backend.

Les permissions sont contrôlées côté backend par Spring Security. Le masquage des liens dans le frontend améliore l’expérience, mais ne remplace pas cette protection.

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
