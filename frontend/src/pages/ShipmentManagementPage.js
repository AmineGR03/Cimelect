import { useEffect, useState } from "react";
import { apiRequest } from "../services/api";

const emptyForm = {
  id: null,
  operationId: "",
  carrier: "",
  departureDate: "",
  expectedArrivalDate: "",
  actualArrivalDate: "",
  status: "EN_PREPARATION",
};

const statuses = ["EN_PREPARATION", "EN_TRANSIT", "ARRIVEE", "LIVREE"];

export default function ShipmentManagementPage() {
  const [shipments, setShipments] = useState([]);
  const [operations, setOperations] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const loadData = async () => {
    try {
      setLoading(true);
      const [shipmentList, imports, exports] = await Promise.all([
        apiRequest("/shipments/all"),
        apiRequest("/operations/import"),
        apiRequest("/operations/export"),
      ]);
      setShipments(shipmentList || []);
      setOperations([...(imports || []), ...(exports || [])]);
    } catch (err) {
      setError(err.message || "Chargement impossible.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const submit = async (event) => {
    event.preventDefault();
    setError("");
    try {
      const payload = {
        operationId: Number(form.operationId),
        carrier: form.carrier || null,
        departureDate: form.departureDate || null,
        expectedArrivalDate: form.expectedArrivalDate || null,
        actualArrivalDate: form.actualArrivalDate || null,
        status: form.status,
      };
      await apiRequest(form.id ? `/shipments/${form.id}` : "/shipments", {
        method: form.id ? "PUT" : "POST",
        body: JSON.stringify(payload),
      });
      setForm(emptyForm);
      await loadData();
    } catch (err) {
      setError(err.message || "Enregistrement impossible.");
    }
  };

  const edit = (shipment) => {
    setForm({
      id: shipment.id,
      operationId: shipment.operationId,
      carrier: shipment.carrier || "",
      departureDate: shipment.departureDate || "",
      expectedArrivalDate: shipment.expectedArrivalDate || "",
      actualArrivalDate: shipment.actualArrivalDate || "",
      status: shipment.status,
    });
  };

  const updateStatus = async (shipment, status) => {
    try {
      await apiRequest(`/shipments/${shipment.id}/status`, {
        method: "PATCH",
        body: JSON.stringify({ status }),
      });
      await loadData();
    } catch (err) {
      setError(err.message || "Mise à jour impossible.");
    }
  };

  return (
    <>
      <div className="page-heading">
        <span className="eyebrow">LOGISTIQUE</span>
        <h1>Gestion des expéditions</h1>
        <p className="text-secondary">Créez et mettez à jour les mouvements rattachés aux opérations.</p>
      </div>
      <div className="row g-4">
        <div className="col-12 col-xl-5">
          <section className="card h-100"><div className="card-body">
            <h2>{form.id ? "Modifier l’expédition" : "Nouvelle expédition"}</h2>
            <form onSubmit={submit} className="management-form mt-3">
              <label className="form-label">Opération</label>
              <select className="form-select mb-3" value={form.operationId} disabled={Boolean(form.id)} required onChange={(e) => setForm({ ...form, operationId: e.target.value })}>
                <option value="">Choisir une opération</option>
                {operations.map((operation) => <option key={operation.id} value={operation.id}>{operation.reference}</option>)}
              </select>
              <label className="form-label">Transporteur</label>
              <input className="form-control mb-3" value={form.carrier} onChange={(e) => setForm({ ...form, carrier: e.target.value })} />
              <div className="row g-3">
                <div className="col-md-6"><label className="form-label">Départ</label><input type="date" className="form-control" value={form.departureDate} onChange={(e) => setForm({ ...form, departureDate: e.target.value })} /></div>
                <div className="col-md-6"><label className="form-label">Arrivée prévue</label><input type="date" className="form-control" value={form.expectedArrivalDate} onChange={(e) => setForm({ ...form, expectedArrivalDate: e.target.value })} /></div>
                <div className="col-md-6"><label className="form-label">Arrivée réelle</label><input type="date" className="form-control" value={form.actualArrivalDate} onChange={(e) => setForm({ ...form, actualArrivalDate: e.target.value })} /></div>
                <div className="col-md-6"><label className="form-label">Statut</label><select className="form-select" value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })}>{statuses.map((status) => <option key={status}>{status}</option>)}</select></div>
              </div>
              {error && <div className="alert alert-danger mt-3">{error}</div>}
              <div className="d-flex gap-2 mt-4"><button className="btn btn-primary" disabled={loading}>{form.id ? "Mettre à jour" : "Créer"}</button>{form.id && <button type="button" className="btn btn-outline-secondary" onClick={() => setForm(emptyForm)}>Annuler</button>}</div>
            </form>
          </div></section>
        </div>
        <div className="col-12 col-xl-7">
          <section className="card h-100"><div className="card-body">
            <div className="d-flex justify-content-between mb-3"><h2>Toutes les expéditions</h2><span className="small text-secondary">{shipments.length} éléments</span></div>
            <div className="table-responsive"><table className="table align-middle"><thead><tr><th>Opération</th><th>Transporteur</th><th>Statut</th><th className="text-end">Actions</th></tr></thead><tbody>
              {shipments.map((shipment) => <tr key={shipment.id}><td>{shipment.operationReference || `#${shipment.operationId}`}</td><td>{shipment.carrier || "—"}</td><td><select className="form-select form-select-sm" value={shipment.status} onChange={(e) => updateStatus(shipment, e.target.value)}>{statuses.map((status) => <option key={status}>{status}</option>)}</select></td><td className="text-end"><button className="btn btn-sm btn-outline-primary" onClick={() => edit(shipment)}>Éditer</button></td></tr>)}
            </tbody></table>{!shipments.length && <p className="text-center text-secondary py-4 mb-0">Aucune expédition trouvée.</p>}</div>
          </div></section>
        </div>
      </div>
    </>
  );
}
