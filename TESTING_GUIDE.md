# 🧪 Guide de Test - Gestion d'Agents

## 🚀 Démarrage de l'Application

### Prérequis
- PostgreSQL 12+ démarré sur localhost:5432
- Base de données `cimelect_db` créée
- Java 21+
- Node.js 18+

### Backend
```bash
cd backend
./mvnw.cmd clean spring-boot:run
```

**Résultat attendu:**
```
[INFO] ... - Started BackendApplication in X seconds
```

Les tables `agents` et `agent_assignments` sont créées automatiquement.

### Frontend
```bash
cd frontend
npm install
npm start
```

**Résultat attendu:**
```
Compiled successfully!
Local: http://localhost:3000
```

---

## 📋 Scénarios de Test

### Test 1: Administrateur assigne un agent

**Étapes:**
1. Aller à http://localhost:3000
2. Se connecter avec:
   - Email: `admin@cimelect.local`
   - Mot de passe: `Admin123!`
3. Cliquer sur "Gestion Agents" dans le menu
4. Vérifier: La page charge avec les agents
5. Cliquer sur "+ Assigner Agent"
6. Sélectionner:
   - Responsable: "Responsable Cimelect"
   - Motif: "Test d'assignation"
7. Cliquer sur "Assigner"
8. Vérifier: Message de succès
9. Voir l'agent avec le responsable assigné

**Logs attendus:**
- AuditLog créé avec action "AGENT_ASSIGN"
- Actor = Admin Cimelect
- Details = "Affectation de l'agent... au responsable..."

---

### Test 2: Responsable consulte ses agents

**Étapes:**
1. Se déconnecter (Déconnexion dans le menu)
2. Se connecter avec:
   - Email: `responsable@cimelect.local`
   - Mot de passe: `Responsable123!`
3. Cliquer sur "Gestion Agents"
4. Vérifier: Voir uniquement l'agent assigné
5. Voir le détail: nom, email, état actif

**Comportement attendu:**
- Pas de bouton "+ Assigner Agent"
- Peut voir "✎" (modifier) et "✕" (désassigner)

---

### Test 3: Consulter l'historique

**Étapes:**
1. Admin → "Gestion Agents"
2. Cliquer sur onglet "Historique"
3. Vérifier: Table avec les assignations
4. Colonnes visibles:
   - Date
   - Agent
   - Action (Assigné/Désassigné)
   - Responsable
   - Effectué par
   - Détails

**Résultat attendu:**
- Voir l'assignation créée en Test 1
- Date = maintenant
- Action = "Assigné"
- Effectué par = "Admin Cimelect"

---

### Test 4: Désassigner un agent

**Étapes:**
1. Admin → "Gestion Agents"
2. Trouver l'agent assigné
3. Cliquer sur "✕" (Désassigner)
4. Cliquer OK dans la confirmation
5. Vérifier: Message de succès
6. Voir l'agent avec badge "Non assigné"

**Logs attendus:**
- Nouvel AuditLog avec action "AGENT_UNASSIGN"
- Actor = Admin Cimelect

---

### Test 5: Vérifier l'audit

**Étapes:**
1. Admin → "Audit"
2. Chercher "AGENT_ASSIGN" dans les logs
3. Vérifier:
   - Utilisateur = "Admin Cimelect"
   - Action = "AGENT_ASSIGN"
   - Entité = "Agent #12" (ou ID)
   - Détails = message d'assignation

---

## 🔍 Cas d'Erreur à Tester

### Test 6: Responsable ne peut pas voir tous les agents

**Étapes:**
1. Responsable connecté
2. Accès direct: http://localhost:3000/agents
3. Vérifier: Voir seulement ses agents
4. Rechercher un autre agent
5. Vérifier: Pas trouvé

---

### Test 7: Sélection invalide

**Étapes:**
1. Admin → "Gestion Agents"
2. Cliquer "+ Assigner Agent"
3. NE PAS sélectionner de responsable
4. Cliquer "Assigner"
5. Vérifier: Message d'erreur

**Erreur attendue:**
```
Veuillez sélectionner un agent et un responsable
```

---

### Test 8: Agent sans manager

**Étapes:**
1. Admin → "Gestion Agents"
2. Créer un nouvel agent (API ou via formulaire si disponible)
3. Vérifier: Badge "Non assigné" sur l'agent

---

## 💾 Base de Données - Requêtes de Test

### Vérifier les agents créés
```sql
SELECT a.id, a.name, a.active, u.email as agent_email, m.email as manager_email
FROM agents a
JOIN users u ON a.user_id = u.id
LEFT JOIN users m ON a.manager_id = m.id;
```

### Vérifier l'historique des assignations
```sql
SELECT aa.id, aa.action, ag.name as agent_name, m.email as manager, u.email as assigned_by, aa.created_at
FROM agent_assignments aa
JOIN agents ag ON aa.agent_id = ag.id
JOIN users m ON aa.manager_id = m.id
JOIN users u ON aa.assigned_by_id = u.id
ORDER BY aa.created_at DESC;
```

### Vérifier les logs d'audit
```sql
SELECT * FROM audit_logs
WHERE entity_type = 'Agent'
ORDER BY created_at DESC;
```

---

## 🧬 Tests API avec cURL

### 1. Récupérer tous les agents (Admin)
```bash
curl -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:8080/api/agents
```

### 2. Récupérer les agents d'un responsable
```bash
curl -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:8080/api/agents/manager/1
```

### 3. Assigner un agent
```bash
curl -X POST http://localhost:8080/api/agents/assign \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "agentId": 1,
    "managerId": 2,
    "reason": "Test d'"'"'assignation"
  }'
```

### 4. Désassigner un agent
```bash
curl -X POST http://localhost:8080/api/agents/1/unassign \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 📊 Checklist de Test

### Frontend
- [ ] Page "Gestion Agents" s'affiche
- [ ] Onglet "Agents" affiche la liste
- [ ] Onglet "Historique" affiche les logs
- [ ] Recherche fonctionne
- [ ] Modal d'assignation s'ouvre
- [ ] Motif optionnel peut être rempli
- [ ] Message de succès s'affiche
- [ ] Message d'erreur s'affiche
- [ ] Permissions respectées par rôle

### Backend
- [ ] Compilation sans erreurs
- [ ] Application démarre
- [ ] Tables créées automatiquement
- [ ] Données de démo créées
- [ ] Endpoints répondent
- [ ] Sécurité appliquée (@PreAuthorize)
- [ ] Logs d'audit générés
- [ ] Base de données mise à jour

### Intégration
- [ ] Assignation crée log d'audit
- [ ] UI mise à jour après action
- [ ] Permissions respectées
- [ ] Historique affiche les actions
- [ ] Responsable ne voit que ses agents
- [ ] Admin voit tous les agents

---

## 🐛 Troubleshooting

### Erreur: "Backend inaccessible"
- Vérifier que Spring Boot démarre
- Vérifier le port 8080
- Vérifier la connexion BD

### Erreur: "Session expirée"
- Se reconnecter
- Vérifier le token JWT

### Erreur: "Accès refusé"
- Vérifier le rôle de l'utilisateur
- Vérifier les permissions

### Agents ne s'affichent pas
- Vérifier la requête API
- Vérifier les logs du backend
- Vérifier la BD (SELECT * FROM agents)

### Logs d'audit manquants
- Vérifier la table audit_logs
- Vérifier que l'action est tracée
- Vérifier les permissions

---

## 📝 Rapports de Test

### Template de rapport
```
Test: [Nom du test]
Date: [Date/Heure]
Utilisateur: [Rôle]
Étapes: [Résumé]
Résultat: [PASS/FAIL]
Notes: [Détails si fail]
```

### Exemple
```
Test: Admin assigne agent
Date: 2026-09-09 10:30:00
Utilisateur: ADMINISTRATEUR
Étapes: Assignation d'agent à responsable via UI
Résultat: PASS
Notes: ✅ Agent assigné, log créé, UI mise à jour
```

---

## 🎯 Validation Finale

Pour confirmer que le système fonctionne:

1. ✅ Admin peut voir et gérer tous les agents
2. ✅ Responsable ne voit que ses agents
3. ✅ Chaque action crée un log d'audit
4. ✅ L'historique affiche les assignations
5. ✅ Les permissions sont respectées
6. ✅ La UI est responsive
7. ✅ Les erreurs sont gérées
8. ✅ La base de données est cohérente

**Tous les tests passent ✅ → Système prêt pour production!**
