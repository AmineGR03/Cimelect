import { useEffect, useState, useCallback } from "react";
import { useSelector } from "react-redux";
import { apiRequest } from "../services/api";

const emptyForm = {
  id: null,
  agentId: null,
  managerId: null,
  reason: "",
};

export default function AgentsPage() {
  const currentUser = useSelector((state) => state.auth.user);
  const [agents, setAgents] = useState([]);
  const [managers, setManagers] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [saving, setSaving] = useState(false);
  const [query, setQuery] = useState("");
  const [showAssignModal, setShowAssignModal] = useState(false);
  const [selectedAgent, setSelectedAgent] = useState(null);
  const [activeTab, setActiveTab] = useState("agents");
  const [assignmentHistory, setAssignmentHistory] = useState([]);
  const [loadingHistory, setLoadingHistory] = useState(false);

  // Load agents managed by the current responsible
  const loadAgents = useCallback(async () => {
    try {
      if (currentUser?.role === "RESPONSABLE") {
        const data = await apiRequest(`/agents/manager/${currentUser.id}`);
        setAgents(data || []);
      } else if (currentUser?.role === "ADMINISTRATEUR") {
        const data = await apiRequest("/agents");
        setAgents(data || []);
      }
    } catch (err) {
      setError(err.message || "Chargement échoué");
    }
  }, [currentUser?.role, currentUser?.id]);

  // Load available managers for assignment
  const loadManagers = useCallback(async () => {
    try {
      const data = await apiRequest("/users");
      const responsibles = data?.filter((u) => u.role === "RESPONSABLE") || [];
      setManagers(responsibles);
    } catch (err) {
      console.error("Erreur chargement responsables", err);
    }
  }, []);

  // Load audit logs for agents
  const loadAssignmentHistory = useCallback(async () => {
    setLoadingHistory(true);
    try {
      const data = await apiRequest("/audit");
      const agentLogs = data?.filter((log) => 
        log.entityType === "Agent" && 
        (log.action === "AGENT_ASSIGN" || log.action === "AGENT_UNASSIGN")
      ) || [];
      setAssignmentHistory(agentLogs);
    } catch (err) {
      console.error("Erreur chargement historique", err);
    } finally {
      setLoadingHistory(false);
    }
  }, []);

  useEffect(() => {
    loadAgents();
    loadManagers();
  }, [loadAgents, loadManagers]);

  useEffect(() => {
    if (activeTab === "history") {
      loadAssignmentHistory();
    }
  }, [activeTab, loadAssignmentHistory]);

  const handleAssign = async () => {
    if (!selectedAgent || !form.managerId) {
      setError("Veuillez sélectionner un agent et un responsable");
      return;
    }

    setSaving(true);
    setError("");
    setSuccess("");

    try {
      await apiRequest("/agents/assign", {
        method: "POST",
        body: JSON.stringify({
          agentId: selectedAgent.id,
          managerId: form.managerId,
          reason: form.reason,
        }),
      });
      setSuccess(`Agent ${selectedAgent.name} assigné avec succès`);
      setForm(emptyForm);
      setSelectedAgent(null);
      setShowAssignModal(false);
      loadAgents();
    } catch (err) {
      setError(err.message || "Erreur lors de l'assignation");
    } finally {
      setSaving(false);
    }
  };

  const handleUnassign = async (agentId) => {
    if (!window.confirm("Êtes-vous sûr de vouloir désassigner cet agent ?")) {
      return;
    }

    setSaving(true);
    setError("");
    setSuccess("");

    try {
      await apiRequest(`/agents/${agentId}/unassign`, {
        method: "POST",
      });
      setSuccess("Agent désassigné avec succès");
      loadAgents();
    } catch (err) {
      setError(err.message || "Erreur lors de la désassignation");
    } finally {
      setSaving(false);
    }
  };

  const getVisibleAgents = () => {
    let filtered = agents;
    return filtered.filter((agent) =>
      [agent.userName, agent.userEmail, agent.name, agent.description]
        .filter(Boolean)
        .join(" ")
        .toLowerCase()
        .includes(query.toLowerCase().trim()),
    );
  };

  const handleOpenAssignModal = (agent) => {
    setSelectedAgent(agent);
    setForm({ ...emptyForm, managerId: agent.managerId || "" });
    setShowAssignModal(true);
  };

  return (
    <>
      <div className="page-heading">
        <span className="eyebrow">GESTION</span>
        <h1>Gestion des Agents</h1>
        <p className="text-secondary">
          Gérez l'affectation des agents aux responsables et consultez l'historique des actions.
        </p>
      </div>

      {error && <div className="alert alert-danger">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      <section className="card">
        <div className="card-header">
          <div className="row align-items-center">
            <div className="col-md-6">
              <ul className="nav nav-tabs" role="tablist">
                <li className="nav-item">
                  <button 
                    className={`nav-link ${activeTab === "agents" ? "active" : ""}`}
                    onClick={() => setActiveTab("agents")}
                  >
                    Agents ({agents.length})
                  </button>
                </li>
                <li className="nav-item">
                  <button 
                    className={`nav-link ${activeTab === "history" ? "active" : ""}`}
                    onClick={() => setActiveTab("history")}
                  >
                    Historique
                  </button>
                </li>
              </ul>
            </div>
            <div className="col-md-6 text-end">
              {currentUser?.role === "ADMINISTRATEUR" && activeTab === "agents" && (
                <button
                  className="btn btn-primary btn-sm"
                  onClick={() => handleOpenAssignModal(null)}
                  disabled={saving}
                >
                  + Assigner Agent
                </button>
              )}
            </div>
          </div>
        </div>
        <div className="card-body">
          {/* Agents Tab */}
          {activeTab === "agents" && (
            <>
              <div className="mb-3">
                <input
                  type="text"
                  className="form-control"
                  placeholder="Rechercher par nom, email..."
                  value={query}
                  onChange={(e) => setQuery(e.target.value)}
                  disabled={saving}
                />
              </div>

              <div className="table-responsive">
                <table className="table align-middle">
                  <thead>
                    <tr>
                      <th>Agent</th>
                      <th>Email</th>
                      <th>Description</th>
                      <th>Responsable Assigné</th>
                      <th>État</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {getVisibleAgents().map((agent) => (
                      <tr key={agent.id}>
                        <td>
                          <strong>{agent.name}</strong>
                          <br />
                          <small className="text-secondary">{agent.userName}</small>
                        </td>
                        <td>{agent.userEmail}</td>
                        <td>{agent.description || "—"}</td>
                        <td>
                          {agent.managerName ? (
                            <>
                              <span>{agent.managerName}</span>
                              <br />
                              <small className="text-secondary">
                                Assigné le {new Date(agent.createdAt).toLocaleDateString("fr-FR")}
                              </small>
                            </>
                          ) : (
                            <span className="badge bg-warning">Non assigné</span>
                          )}
                        </td>
                        <td>
                          <span className={`badge bg-${agent.active ? "success" : "secondary"}`}>
                            {agent.active ? "Actif" : "Inactif"}
                          </span>
                        </td>
                        <td>
                          <button
                            className="btn btn-sm btn-info"
                            onClick={() => handleOpenAssignModal(agent)}
                            disabled={saving}
                            title="Assigner à un responsable"
                          >
                            ✎
                          </button>
                          {agent.managerId && (
                            <button
                              className="btn btn-sm btn-danger"
                              onClick={() => handleUnassign(agent.id)}
                              disabled={saving}
                              title="Désassigner"
                            >
                              ✕
                            </button>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
                {!getVisibleAgents().length && !error && (
                  <p className="text-center text-secondary py-4 mb-0">Aucun agent trouvé.</p>
                )}
              </div>
            </>
          )}

          {/* History Tab */}
          {activeTab === "history" && (
            <div className="table-responsive">
              {loadingHistory ? (
                <p className="text-center text-secondary">Chargement de l'historique...</p>
              ) : (
                <table className="table align-middle">
                  <thead>
                    <tr>
                      <th>Date</th>
                      <th>Agent</th>
                      <th>Action</th>
                      <th>Responsable</th>
                      <th>Effectué par</th>
                      <th>Détails</th>
                    </tr>
                  </thead>
                  <tbody>
                    {assignmentHistory.map((log) => (
                      <tr key={log.id}>
                        <td>{new Date(log.createdAt).toLocaleString("fr-FR")}</td>
                        <td>Agent #{log.entityId}</td>
                        <td>
                          <span className={`badge bg-${log.action === "AGENT_ASSIGN" ? "success" : "warning"}`}>
                            {log.action === "AGENT_ASSIGN" ? "Assigné" : "Désassigné"}
                          </span>
                        </td>
                        <td>{log.details?.includes("responsable") ? log.details.split("responsable ")[1]?.split("\n")[0] : "—"}</td>
                        <td>{log.actorName || log.actorEmail || "—"}</td>
                        <td>{log.details || "—"}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
              {!assignmentHistory.length && !loadingHistory && (
                <p className="text-center text-secondary py-4 mb-0">Aucune action d'assignation enregistrée.</p>
              )}
            </div>
          )}
        </div>
      </section>

      {/* Assignment Modal */}
      {showAssignModal && (
        <div className="modal" style={{ display: "block", backgroundColor: "rgba(0,0,0,0.5)" }}>
          <div className="modal-dialog">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title">
                  {selectedAgent ? `Assigner: ${selectedAgent.name}` : "Assigner un Agent"}
                </h5>
                <button
                  type="button"
                  className="btn-close"
                  onClick={() => {
                    setShowAssignModal(false);
                    setSelectedAgent(null);
                  }}
                  disabled={saving}
                ></button>
              </div>
              <div className="modal-body">
                <div className="mb-3">
                  <label className="form-label">Responsable</label>
                  <select
                    className="form-select"
                    value={form.managerId}
                    onChange={(e) => setForm({ ...form, managerId: e.target.value })}
                    disabled={saving}
                  >
                    <option value="">Sélectionner un responsable</option>
                    {managers.map((manager) => (
                      <option key={manager.id} value={manager.id}>
                        {manager.firstName} {manager.lastName} ({manager.email})
                      </option>
                    ))}
                  </select>
                </div>

                <div className="mb-3">
                  <label className="form-label">Motif (optionnel)</label>
                  <textarea
                    className="form-control"
                    rows="3"
                    value={form.reason}
                    onChange={(e) => setForm({ ...form, reason: e.target.value })}
                    placeholder="Expliquez le motif de l'assignation..."
                    disabled={saving}
                  ></textarea>
                </div>
              </div>
              <div className="modal-footer">
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={() => {
                    setShowAssignModal(false);
                    setSelectedAgent(null);
                  }}
                  disabled={saving}
                >
                  Annuler
                </button>
                <button
                  type="button"
                  className="btn btn-primary"
                  onClick={handleAssign}
                  disabled={saving}
                >
                  {saving ? "En cours..." : "Assigner"}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
