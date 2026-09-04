import { useState } from "react";
import { useSelector } from "react-redux";
import { apiRequest } from "../services/api";
export default function PartnersPage() {
  const partners = useSelector((state) => state.dashboard.data.partners || []);
  const [details, setDetails] = useState(null);
  const [error, setError] = useState("");
  const [query, setQuery] = useState("");
  const visiblePartners = partners.filter((item) => [item.companyName, item.country, item.contactName, item.kind].filter(Boolean).join(" ").toLowerCase().includes(query.toLowerCase().trim()));

  const showDetails = async (partner) => {
    try {
      setError("");
      const base = partner.kind === "Fournisseur" ? "suppliers" : "customers";
      const [operations, indicators] = await Promise.all([
        apiRequest(`/${base}/${partner.id}/operations`),
        apiRequest(`/${base}/${partner.id}/indicators`),
      ]);
      setDetails({ partner, operations: operations || [], indicators });
    } catch (err) {
      setError(err.message || "Détails indisponibles.");
    }
  };
  return (
    <>
      <div className="page-heading">
        <span className="eyebrow">ÉCOSYSTÈME</span>
        <h1>Partenaires</h1>
        <p className="text-secondary">
          Clients et fournisseurs connectés à vos flux.
        </p>
      </div>
      {error && <div className="alert alert-danger">{error}</div>}
      <input className="form-control mb-3" placeholder="Rechercher un partenaire..." value={query} onChange={(e) => setQuery(e.target.value)} />
      <div className="row g-3">
        {visiblePartners.map((item) => (
          <div
            className="col-12 col-md-6 col-xl-4"
            key={`${item.kind}-${item.id}`}
          >
            <article className="card partner-card h-100">
              <div className="card-body d-flex align-items-start gap-3">
                <div className="partner-avatar">
                  {item.companyName?.slice(0, 2).toUpperCase()}
                </div>
                <div>
                  <span className="eyebrow">{item.kind}</span>
                  <h3>{item.companyName}</h3>
                  <p className="text-secondary mb-1">
                    {item.country || "Pays non renseigné"}
                  </p>
                  <small>{item.contactName || "Contact non renseigné"}</small>
                  <button className="btn btn-sm btn-outline-primary d-block mt-3" onClick={() => showDetails(item)}>Voir le détail</button>
                </div>
              </div>
            </article>
          </div>
        ))}
        {!visiblePartners.length && (
          <p className="text-secondary">Aucun partenaire trouvé.</p>
        )}
      </div>
      {details && <section className="card mt-4"><div className="card-body">
        <div className="d-flex justify-content-between align-items-center"><div><span className="eyebrow">FICHE PARTENAIRE</span><h2>{details.partner.companyName}</h2></div><button className="btn btn-sm btn-outline-secondary" onClick={() => setDetails(null)}>Fermer</button></div>
        <div className="row g-3 mt-1"><div className="col-md-4"><span className="text-secondary">Opérations</span><div className="big-metric">{details.indicators?.operationCount ?? details.operations.length}</div></div><div className="col-md-4"><span className="text-secondary">Montant total</span><div className="big-metric">{details.indicators?.totalAmount ?? "—"}</div></div><div className="col-md-4"><span className="text-secondary">Anomalies</span><div className="big-metric">{details.indicators?.anomalyCount ?? "—"}</div></div></div>
        <h3 className="mt-4">Opérations associées</h3><div className="table-responsive"><table className="table"><thead><tr><th>Référence</th><th>Type</th><th>Statut</th></tr></thead><tbody>{details.operations.map((operation) => <tr key={operation.id}><td>{operation.reference}</td><td>{operation.type}</td><td>{operation.status}</td></tr>)}</tbody></table>{!details.operations.length && <p className="text-secondary">Aucune opération.</p>}</div>
      </div></section>}
    </>
  );
}
