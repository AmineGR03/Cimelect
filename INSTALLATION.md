# Installation de Cimelect

Ce guide explique comment installer et lancer Cimelect en environnement local Windows.

## 1. Prérequis

Installer les outils suivants :

- JDK 21.
- Node.js et npm.
- PostgreSQL 17 ou compatible.
- Git, si le projet est récupéré depuis un dépôt.

Vérifier les installations :

```powershell
java -version
node --version
npm --version
psql --version
```

## 2. Préparer PostgreSQL

Démarrer le service PostgreSQL, puis créer la base de données :

```sql
CREATE DATABASE cimelect_db;
```

La configuration actuelle utilise :

| Paramètre | Valeur |
| --- | --- |
| Hôte | `localhost` |
| Port | `5432` |
| Base | `cimelect_db` |
| Utilisateur | `postgres` |
| Mot de passe | `123456789` |

Ces valeurs peuvent être modifiées dans `backend/src/main/resources/application.properties`.

## 3. Installer et lancer le backend

Depuis la racine du projet :

```powershell
cd backend
$env:JAVA_HOME="C:\Program Files\Java\jdk-21.0.12"
.\mvnw.cmd clean compile
.\mvnw.cmd spring-boot:run
```

L’API démarre sur :

```text
http://localhost:8080
```

Au premier démarrage, le backend crée automatiquement les tables grâce à JPA et initialise :

- le compte administrateur bootstrap ;
- les comptes de démonstration responsable et agent import/export ;
- les exigences documentaires des imports et exports.

Compte de connexion :

```text
Email : admin@cimelect.local
Mot de passe : Admin123!
```

Les autres comptes et les données métier peuvent être créés depuis l’application ou les endpoints protégés.

## 4. Installer et lancer le frontend

Ouvrir un second terminal à la racine du projet :

```powershell
cd frontend
npm install
npm start
```

L’interface démarre sur :

```text
http://localhost:3000
```

En développement, le frontend utilise le proxy défini dans `frontend/package.json` pour transmettre `/api` vers le backend sur le port `8080`.

## 5. Première connexion

1. Ouvrir `http://localhost:3000`.
2. Saisir `admin@cimelect.local`.
3. Saisir `Admin123!`.
4. Accéder au tableau de bord et aux fonctionnalités disponibles pour le rôle `ADMINISTRATEUR`.

Le token JWT est conservé dans le stockage local du navigateur.

## 6. Vérifier l’installation

Tester le frontend :

```powershell
cd frontend
npm test -- --watchAll=false
npm run build
```

Tester la connexion à l’API :

```powershell
$body = @{ email = 'admin@cimelect.local'; password = 'Admin123!' } | ConvertTo-Json
Invoke-RestMethod -Uri http://localhost:3000/api/auth/login -Method Post -ContentType 'application/json' -Body $body
```

La réponse doit contenir un token, l’adresse email, l’identifiant utilisateur et le rôle.

Tester le backend avec PostgreSQL actif :

```powershell
cd backend
$env:JAVA_HOME="C:\Program Files\Java\jdk-21.0.12"
.\mvnw.cmd test
```

## 7. Dépannage

### Le port 8080 est déjà utilisé

Identifier le processus :

```powershell
Get-NetTCPConnection -LocalPort 8080 -State Listen
```

Arrêter le processus concerné avec son identifiant :

```powershell
Stop-Process -Id <PID> -Force
```

Puis relancer le backend.

### Le port 3000 est déjà utilisé

Identifier le processus :

```powershell
Get-NetTCPConnection -LocalPort 3000 -State Listen
```

Arrêter le processus concerné, puis relancer :

```powershell
cd frontend
npm start
```

### Erreur 404 sur `/api/auth/login`

Arrêter puis relancer le frontend. Le proxy de `package.json` n’est chargé qu’au démarrage du serveur React.

Vérifier aussi que le backend tourne sur `http://localhost:8080`.

### Erreur de connexion PostgreSQL

Vérifier que :

- PostgreSQL est démarré ;
- la base `cimelect_db` existe ;
- le mot de passe de l’utilisateur `postgres` correspond à `application.properties`.

### Le mot de passe admin est refusé

Redémarrer le backend. Le `DataInitializer` réaligne le compte bootstrap avec les valeurs configurées lorsque le mot de passe enregistré ne correspond pas.

## 8. Arrêt des serveurs

Dans chaque terminal où un serveur tourne, utiliser :

```text
Ctrl + C
```

## Sécurité

Les identifiants présents dans ce guide sont uniquement destinés au développement local. Avant tout déploiement :

- modifier le mot de passe administrateur ;
- modifier le secret JWT ;
- modifier le mot de passe PostgreSQL ;
- utiliser des variables d’environnement ou un gestionnaire de secrets.
