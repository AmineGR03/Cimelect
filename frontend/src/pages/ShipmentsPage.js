import { useState } from "react";
import { useSelector } from "react-redux";
import StatusBadge from "../components/StatusBadge";
export default function ShipmentsPage() {
  const [query, setQuery] = useState("");
  const shipments = useSelector(
    (state) => state.dashboard.data.shipments || [],
  );
  const visibleShipments = shipments.filter((item) => [item.operationReference, item.carrier, item.status].filter(Boolean).join(" ").toLowerCase().includes(query.toLowerCase().trim()));
  return (
    <>
      <div className="page-heading">
        <span className="eyebrow">LOGISTIQUE</span>
        <h1>Expéditions</h1>
        <p className="text-secondary">
          Les mouvements à surveiller aujourd’hui.
        </p>
      </div>
      <input className="form-control mb-3" placeholder="Rechercher une expédition..." value={query} onChange={(e) => setQuery(e.target.value)} />
      <div className="row g-3">
        {visibleShipments.map((item) => (
          <div className="col-12 col-lg-6" key={item.id}>
            <article className="card shipment-card">
              <div className="card-body d-flex gap-3 align-items-start">
                <div className="shipment-icon">
                  <i className="bi bi-box-seam"></i>
                </div>
                <div className="flex-grow-1">
                  <span className="small text-secondary">
                    {item.operationReference ||
                      `Opération #${item.operationId}`}
                  </span>
                  <h3>{item.carrier || "Transporteur à confirmer"}</h3>
                  <p className="mb-2">
                    Départ {item.departureDate || "—"} <b>→</b> arrivée prévue{" "}
                    {item.expectedArrivalDate || "—"}
                  </p>
                  <StatusBadge status={item.status} />
                </div>
              </div>
            </article>
          </div>
        ))}
        {!visibleShipments.length && (
          <p className="text-secondary">Aucune expédition en cours.</p>
        )}
      </div>
    </>
  );
}
