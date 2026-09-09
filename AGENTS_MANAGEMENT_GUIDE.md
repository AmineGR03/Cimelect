# Guide d'Utilisation - Gestion d'Agents

## Vue d'ensemble

Le système de gestion d'agents permet aux **Responsables** et **Administrateurs** de gérer l'affectation des agents aux responsables, avec un suivi complet des actions via les logs d'audit.

## Architecture

### Entités Principales

#### 1. **Agent**
```java
- id: Long (clé primaire)
- user: User (référence à l'utilisateur agent)
- manager: User (responsable assigné)
- name: String (nom de l'agent)
- description: String (optionnel)
- active: boolean (état actif/inactif)
- createdAt, updatedAt: Instant (timestamps)
```

#### 2. **AgentAssignment**
```java
- id: Long (clé primaire)
- agent: Agent (agent affecté)
- manager: User (responsable destinataire)
- assignedBy: User (qui a fait l'affectation)
- action: AuditAction (AGENT_ASSIGN / AGENT_UNASSIGN)
- reason: String (motif de l'affectation)
- createdAt: Instant (date de l'action)
```

### Flux d'Utilisation

#### Pour un Administrateur

1. **Accès**: Menu latéral → "Gestion Agents"
2. **Actions possibles**:
   - Voir tous les agents du système
   - Assigner un agent à un responsable
   - Désassigner un agent
   - Consulter l'historique des assignations

#### Pour un Responsable

1. **Accès**: Menu latéral → "Gestion Agents"
2. **Actions possibles**:
   - Voir ses agents assignés
   - Consulter l'historique des assignations
   - Modifier l'assignation de ses agents

### Logs d'Audit

Chaque action de gestion d'agent génère une entrée dans `AuditLog`:

```
entityType: "Agent"
action: AGENT_ASSIGN | AGENT_UNASSIGN | CREATE | UPDATE | DELETE
actor: Utilisateur qui a effectué l'action
details: "Affectation de l'agent X au responsable Y"
createdAt: Timestamp de l'action
```

**Consulter les logs**: Page Audit (Menu → Audit)

## Endpoints API

### Agents

#### GET /api/agents
- **Permission**: ADMINISTRATEUR
- **Retour**: Liste de tous les agents

#### GET /api/agents/{id}
- **Permission**: ADMINISTRATEUR, RESPONSABLE
- **Retour**: Détails d'un agent

#### GET /api/agents/manager/{managerId}
- **Permission**: ADMINISTRATEUR, ou RESPONSABLE si {managerId} = ID courant
- **Retour**: Agents assignés au responsable

#### GET /api/agents/manager/{managerId}/active
- **Permission**: ADMINISTRATEUR, ou RESPONSABLE si {managerId} = ID courant
- **Retour**: Agents actifs assignés au responsable

#### POST /api/agents
- **Permission**: ADMINISTRATEUR
- **Body**: AgentRequest
- **Retour**: AgentResponse créé

#### PUT /api/agents/{id}
- **Permission**: ADMINISTRATEUR, RESPONSABLE
- **Body**: AgentRequest
- **Retour**: AgentResponse modifié

#### POST /api/agents/assign
- **Permission**: ADMINISTRATEUR, RESPONSABLE
- **Body**: 
```json
{
  "agentId": 1,
  "managerId": 2,
  "reason": "Motif optionnel"
}
```
- **Retour**: 200 OK

#### POST /api/agents/{id}/unassign
- **Permission**: ADMINISTRATEUR, RESPONSABLE
- **Retour**: 200 OK

#### DELETE /api/agents/{id}
- **Permission**: ADMINISTRATEUR
- **Retour**: 204 No Content

## Initialisation des Données

Lors du démarrage de l'application:
1. Les utilisateurs de démo sont créés (admin, responsable, agent)
2. Un agent est automatiquement créé et assigné au responsable
3. Les tables `agents` et `agent_assignments` sont créées par Hibernate

## Utilisateurs de Démonstration

| Email | Mot de passe | Rôle | Droits |
|-------|--------------|------|--------|
| admin@cimelect.local | Admin123! | ADMINISTRATEUR | Gestion complète des agents |
| responsable@cimelect.local | Responsable123! | RESPONSABLE | Gestion de ses agents |
| agent@cimelect.local | Agent123! | AGENT_IMPORT_EXPORT | Voir ses informations |

## Scénarios d'Utilisation

### Scénario 1: Assigner un nouvel agent à un responsable

1. Admin se connecte
2. Accède à "Gestion Agents"
3. Clique "Assigner Agent"
4. Sélectionne l'agent et le responsable
5. Ajoute un motif (optionnel)
6. Valide
7. **Log créé**: "Affectation de l'agent X au responsable Y"

### Scénario 2: Responsable consulte ses agents

1. Responsable se connecte
2. Accède à "Gestion Agents"
3. Voit la liste de ses agents assignés
4. Peut voir le statut (actif/inactif)
5. Peut consulter l'historique

### Scénario 3: Désassigner un agent

1. Admin sélectionne un agent
2. Clique le bouton "✕" (Désassigner)
3. Confirme l'action
4. **Log créé**: "Suppression de l'agent X du responsable Y"

## Sécurité

- Les logs d'audit sont immuables (colonne `updatable = false`)
- Les responsables ne voient que leurs propres agents
- Les administrateurs ont accès complet
- Les actions sont tracées avec l'utilisateur acteur

## Prochaines Améliorations

- Filtrage avancé des agents par statut/date
- Export des logs d'audit en CSV/PDF
- Notifications lors des assignations
- Rapport d'activité des responsables
- Délégation de pouvoir entre responsables
