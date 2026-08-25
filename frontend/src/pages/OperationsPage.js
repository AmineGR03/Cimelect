import { useSelector } from "react-redux";
import StatusBadge from "../components/StatusBadge";
export default function OperationsPage() {
  const operations = useSelector(
    (state) => state.dashboard.data.operations || [],
  );
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
            <button className="btn btn-sm btn-outline-secondary">
              <i className="bi bi-funnel me-2"></i>Filtrer
            </button>
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
                {operations.map((item) => (
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
            {!operations.length && (
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
