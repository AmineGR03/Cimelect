# Audit CDC - Checklist fonctionnelle

> État du projet vérifié le 4 septembre 2026. Référence : `CDC.pdf` à la racine.

## Fonctionnalités couvertes

- [x] Authentification par JWT.
- [x] Déconnexion côté frontend et backend.
- [x] Gestion des rôles `ADMINISTRATEUR`, `RESPONSABLE` et `AGENT_IMPORT_EXPORT` côté backend.
- [x] Gestion des utilisateurs côté backend.
- [x] Écran frontend de gestion des utilisateurs pour l’administrateur.
- [x] Création, modification et archivage des fournisseurs.
- [x] Création, modification et archivage des clients.
- [x] Création, modification et suppression des produits.
- [x] Création des opérations d’import et d’export.
- [x] Modification et suppression logique des opérations.
- [x] Cycles de statuts distincts pour les imports et les exports.
- [x] Contrôle du coût réel et de la date de réception avant clôture d’une opération.
- [x] Contrôle des documents obligatoires avant clôture.
- [x] Création et modification des expéditions côté API.
- [x] Synchronisation du statut d’une expédition avec celui de son opération.
- [x] Détection d’un dépassement de délai côté backend.
- [x] Journalisation backend des créations, modifications, changements de statut, suppressions et clôtures.
- [x] API de consultation des opérations et indicateurs par partenaire.
- [x] API de gestion des documents : upload, téléchargement, suppression et exigences documentaires.
- [x] Écran frontend de gestion des documents par opération.
- [x] Upload, téléchargement et suppression confirmée depuis le frontend.
- [x] Écran frontend de gestion des expéditions : création, modification et statut.
- [x] Actions frontend de transition et de clôture des opérations.
- [x] Journal d’audit consultable depuis le frontend.
- [x] Consultation des opérations et indicateurs depuis la fiche partenaire.
- [x] Page frontend de profil et modification des informations personnelles.
- [x] Tableau de bord avec KPI, tendances import/export et alertes.
- [x] Interface frontend responsive compilable.

## Fonctionnalités partielles

- [x] **Tableau de bord** : les taux de conformité documentaire sont calculés par le backend à partir des documents réels.
- [ ] **Analyse IA** : la détection est simulée par `aiAnalysisTriggered` et un message fixe ; aucun moteur IA, résultat détaillé ou historique d’analyse n’est intégré.
- [x] **Opérations** : l’historique détaillé est consultable depuis la gestion des opérations.
- [x] **Expéditions** : un endpoint `/all` permet d’administrer également les expéditions livrées.
- [x] **Partenaires** : les opérations et indicateurs sont consultables ; une recherche est disponible dans la liste.

## Fonctionnalités manquantes à implémenter

### Priorité haute

- [x] Corriger le chargement de l’espace pour le rôle `RESPONSABLE` : ce rôle ne doit pas appeler les endpoints fournisseurs/clients qui lui sont interdits.
- [x] Empêcher le rôle `AGENT_IMPORT_EXPORT` d’ouvrir le dashboard auquel il n’a pas accès.

### Priorité moyenne

- [x] Ajouter les fiches détaillées fournisseurs et clients.
- [x] Afficher les opérations et indicateurs associés à chaque partenaire.
- [x] Ajouter une page de consultation de l’audit pour les rôles autorisés.
- [x] Ajouter une page de profil et la modification des informations personnelles.
- [x] Remplacer les métriques documentaires codées en dur par des calculs issus de l’API.
- [x] Ajouter des validations frontend cohérentes avec les contraintes backend : lignes produit, quantités, prix et partenaires obligatoires.
- [x] Empêcher la suppression de la dernière ligne produit dans une opération.
- [x] Ajouter des états de chargement, d’erreur et de succès sur les principaux écrans métier.

### Priorité basse / amélioration

- [x] Ajouter recherche, filtres, tri et pagination aux principales listes de gestion.
- [ ] Ajouter une vraie gestion des alertes IA : règles explicites, détail, statut traité/non traité et historique (reporté volontairement).
- [x] Ajouter des tests frontend ciblés pour l’authentification, les rôles, opérations, expéditions, documents et utilisateurs.
- [ ] Étendre les tests backend aux autorisations, transitions de statut, clôture et documents obligatoires.
- [ ] Ajouter des tests d’intégration avec PostgreSQL pour les parcours principaux ; le test de démarrage et d’initialisation passe déjà avec PostgreSQL actif.

## Corrections techniques à prévoir

- [ ] Configurer Maven pour utiliser automatiquement un JDK 21 sur tous les environnements ; la compilation et les tests passent avec `JAVA_HOME` positionné sur JDK 21.
- [x] Mettre à jour `CREDENTIALS.md` : `DataInitializer` crée désormais aussi automatiquement les comptes responsable et agent.
- [ ] Ne jamais conserver les mots de passe, le secret JWT et les identifiants PostgreSQL de démonstration pour un déploiement de production.
- [x] Vérifier et expliciter les autorisations de consultation et téléchargement des documents pour les trois rôles métier.

## Vérifications effectuées

- [x] Le build frontend `npm run build` passe.
- [x] Le test frontend existant passe.
- [x] Exécuter les tests backend avec PostgreSQL démarré sur `localhost:5432` ; compilation et tests validés avec Java 21.
- [ ] Les parcours complets doivent être testés avec PostgreSQL, le backend et le frontend démarrés ensemble.
- [ ] Tester les trois rôles séparément en conditions réelles sur les écrans et les endpoints autorisés.

## Améliorations globales restantes

- [ ] Ajouter une gestion centralisée des erreurs réseau et des sessions expirées.
- [ ] Ajouter des confirmations et retours accessibles pour toutes les actions destructives.
- [ ] Compléter l’accessibilité : associations labels/champs, navigation clavier et annonces des statuts.
- [ ] Ajouter tri, filtres et pagination côté API pour les gros volumes de données.
- [ ] Ajouter des logs structurés, une surveillance de santé et des métriques de production.
- [ ] Ajouter une documentation OpenAPI des endpoints et des contrats DTO.
- [ ] Ajouter sauvegardes PostgreSQL, stratégie de restauration et procédure de déploiement.
- [ ] Remplacer les secrets de développement par des variables d’environnement en production.
- [ ] Vérifier les limites de taille/type des fichiers et sécuriser le stockage documentaire.
- [ ] Ajouter une stratégie de concurrence pour la génération des références d’opérations.
- [ ] Ajouter des tests end-to-end navigateur pour les trois rôles.
