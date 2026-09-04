import { useState } from "react";
import { useSelector } from "react-redux";
import StatusBadge from "../components/StatusBadge";
export default function OperationsPage() {
  const [query, setQuery] = useState("");
  const [type, setType] = useState("ALL");
  const [sort, setSort] = useState("reference");
  const operations = useSelector(
    (state) => state.dashboard.data.operations || [],
  );
  const filteredOperations = operations.filter((item) => {
    const text = [item.reference, item.supplierName, item.customerName, item.destination, item.status].filter(Boolean).join(" ").toLowerCase();
    return (type === "ALL" || item.type === type) && text.includes(query.toLowerCase().trim());
  }).sort((left, right) => String(left[sort] || "").localeCompare(String(right[sort] || "")));
  return (
    <>
      <div className="page-heading">
        <span className="eyebrow">FLUX INTERNATIONAUX</span>
        <h1>Opérations</h1>
        <p className="text-secondary">
          Suivez les imports et exports du réseau.
        </p>
      </div>
      <section className="card">
        <div className="card-body">
          <div className="d-flex justify-content-between mb-3">
            <strong>{operations.length} dossiers</strong>
            <div className="d-flex gap-2"><input className="form-control form-control-sm" placeholder="Rechercher..." value={query} onChange={(e) => setQuery(e.target.value)} /><select className="form-select form-select-sm" value={type} onChange={(e) => setType(e.target.value)}><option value="ALL">Tous</option><option value="IMPORT">Imports</option><option value="EXPORT">Exports</option></select><select className="form-select form-select-sm" value={sort} onChange={(e) => setSort(e.target.value)}><option value="reference">Référence</option><option value="status">Statut</option><option value="type">Type</option></select></div>
          </div>
          <div className="table-responsive">
            <table className="table align-middle">
              <thead>
                <tr>
                  <th>Référence</th>
                  <th>Type</th>
                  <th>Partenaire</th>
                  <th>Destination</th>
                  <th>Échéance</th>
                  <th>Statut</th>
                </tr>
              </thead>
              <tbody>
                {filteredOperations.map((item) => (
                  <tr key={item.id}>
                    <td className="fw-semibold">
                      {item.reference || `OP-${item.id}`}
                    </td>
                    <td>
                      <span
                        className={`badge ${item.type === "IMPORT" ? "text-bg-primary" : "text-bg-warning"}`}
                      >
                        {item.type}
                      </span>
                    </td>
                    <td>{item.supplierName || item.customerName || "—"}</td>
                    <td>{item.destination || "—"}</td>
                    <td>{item.expectedDate || "—"}</td>
                    <td>
                      <StatusBadge status={item.status} />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            {!filteredOperations.length && (
              <p className="text-center text-secondary py-4">
                Aucune opération trouvée.
              </p>
            )}
          </div>
        </div>
      </section>
    </>
  );
}
