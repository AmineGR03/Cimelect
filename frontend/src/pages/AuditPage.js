import { useEffect, useState } from "react";
import { apiRequest } from "../services/api";

export default function AuditPage() {
  const [logs, setLogs] = useState([]);
  const [error, setError] = useState("");

  useEffect(() => {
    apiRequest("/audit")
      .then((data) => setLogs(data || []))
      .catch((err) => setError(err.message || "Chargement impossible."));
  }, []);

  return (
    <>
      <div className="page-heading"><span className="eyebrow">CONTRÔLE</span><h1>Journal d’audit</h1><p className="text-secondary">Historique des actions enregistrées dans l’espace de travail.</p></div>
      <section className="card"><div className="card-body">
        {error && <div className="alert alert-danger">{error}</div>}
        <div className="table-responsive"><table className="table align-middle"><thead><tr><th>Date</th><th>Utilisateur</th><th>Action</th><th>Objet</th><th>Détails</th></tr></thead><tbody>
          {logs.map((log) => <tr key={log.id}><td>{log.createdAt ? new Date(log.createdAt).toLocaleString("fr-FR") : "—"}</td><td>{log.actorName || log.actorEmail || "—"}</td><td>{log.action}</td><td>{log.entityType} #{log.entityId}</td><td>{log.details || "—"}</td></tr>)}
        </tbody></table>{!logs.length && !error && <p className="text-center text-secondary py-4 mb-0">Aucune action enregistrée.</p>}</div>
      </div></section>
    </>
  );
}
