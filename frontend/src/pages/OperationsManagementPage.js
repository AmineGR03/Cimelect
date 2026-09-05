import { useCallback, useEffect, useState } from "react";
import { useSelector } from "react-redux";
import { apiRequest } from "../services/api";

const emptyForm = {
  id: null,
  type: "IMPORT",
  destination: "",
  carrier: "",
  supplierId: "",
  customerId: "",
  orderDate: "",
  expectedDate: "",
  plannedCost: "",
  actualCost: "",
  lines: [
    { productId: "", quantity: "", unitPrice: "" },
  ],
};

const nextStatuses = {
  IMPORT: { CREEE: "EN_TRANSIT", EN_TRANSIT: "DEDOUANEMENT", DEDOUANEMENT: "RECUE" },
  EXPORT: { CREEE: "PREPARATION", PREPARATION: "EXPEDIEE", EXPEDIEE: "LIVREE" },
};

export default function OperationsManagementPage() {
  const user = useSelector((state) => state.auth.user);
  const canEditOperations = ["ADMINISTRATEUR", "AGENT_IMPORT_EXPORT"].includes(user?.role);
  const canCloseOrDelete = ["ADMINISTRATEUR", "RESPONSABLE"].includes(user?.role);
  const [operations, setOperations] = useState([]);
  const [suppliers, setSuppliers] = useState([]);
  const [customers, setCustomers] = useState([]);
  const [products, setProducts] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [error, setError] = useState("");
  const [history, setHistory] = useState(null);

  const loadData = useCallback(async () => {
    try {
      const [imports, exports, supplierList, customerList, productList] = await Promise.all([
        apiRequest("/operations/import"),
        apiRequest("/operations/export"),
        canEditOperations ? apiRequest("/suppliers") : Promise.resolve([]),
        canEditOperations ? apiRequest("/customers") : Promise.resolve([]),
        canEditOperations ? apiRequest("/products") : Promise.resolve([]),
      ]);
      setOperations([...imports, ...exports]);
      setSuppliers(supplierList || []);
      setCustomers(customerList || []);
      setProducts(productList || []);
    } catch (err) {
      setError(err.message || "Impossible de charger les données.");
    }
  }, [canEditOperations]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const updateLine = (index, field, value) => {
    const next = [...form.lines];
    next[index] = { ...next[index], [field]: value };
    setForm({ ...form, lines: next });
  };

  const addLine = () => {
    setForm({
      ...form,
      lines: [...form.lines, { productId: "", quantity: "", unitPrice: "" }],
    });
  };

  const removeLine = (index) => {
    if (form.lines.length === 1) return;
    setForm({
      ...form,
      lines: form.lines.filter((_, idx) => idx !== index),
    });
  };

  const submit = async (event) => {
    event.preventDefault();
    setError("");
    if (form.type === "IMPORT" && !form.supplierId) {
      setError("Le fournisseur est obligatoire.");
      return;
    }
    if (form.type === "EXPORT" && !form.customerId) {
      setError("Le client est obligatoire.");
      return;
    }
    if (form.lines.some((line) => !line.productId || Number(line.quantity) <= 0 || (line.unitPrice !== "" && Number(line.unitPrice) < 0))) {
      setError("Chaque ligne doit contenir un produit, une quantité positive et un prix valide.");
      return;
    }
    const payload = {
      supplierId: form.type === "IMPORT" ? Number(form.supplierId) : null,
      customerId: form.type === "EXPORT" ? Number(form.customerId) : null,
      destination: form.destination,
      orderDate: form.orderDate || null,
      expectedDate: form.expectedDate || null,
      carrier: form.carrier,
      plannedCost: form.plannedCost ? Number(form.plannedCost) : null,
      actualCost: form.actualCost ? Number(form.actualCost) : null,
      lines: form.lines.map((line) => ({
        productId: Number(line.productId),
        quantity: Number(line.quantity),
        unitPrice: line.unitPrice ? Number(line.unitPrice) : null,
      })),
    };

    try {
      if (form.id) {
        await apiRequest(`/operations/${form.id}`, {
          method: "PUT",
          body: JSON.stringify(payload),
        });
      } else {
        await apiRequest(`/operations/${form.type.toLowerCase()}`, {
          method: "POST",
          body: JSON.stringify(payload),
        });
      }
      setForm(emptyForm);
      await loadData();
    } catch (err) {
      setError(err.message || "Impossible de sauvegarder cette opération.");
    }
  };

  const removeOperation = async (id) => {
    if (!window.confirm("Supprimer cette opération ?")) return;
    try {
      await apiRequest(`/operations/${id}`, {
        method: "DELETE",
        body: JSON.stringify({ justification: "Suppression depuis le front" }),
      });
      await loadData();
    } catch (err) {
      setError(err.message || "Suppression impossible.");
    }
  };

  const editOperation = (operation) => {
    setForm({
      id: operation.id,
      type: operation.type,
      destination: operation.destination || "",
      carrier: operation.carrier || "",
      supplierId: operation.supplierId || "",
      customerId: operation.customerId || "",
      orderDate: operation.orderDate || "",
      expectedDate: operation.expectedDate || "",
      plannedCost: operation.plannedCost ?? "",
      actualCost: operation.actualCost ?? "",
      lines: (operation.lines || []).map((line) => ({
        productId: line.productId || "",
        quantity: line.quantity ?? "",
        unitPrice: line.unitPrice ?? "",
      })),
    });
  };

  const advanceStatus = async (operation) => {
    const status = nextStatuses[operation.type]?.[operation.status];
    if (!status) return;
    try {
      await apiRequest(`/operations/${operation.id}/status`, {
        method: "PATCH",
        body: JSON.stringify({ status }),
      });
      await loadData();
    } catch (err) {
      setError(err.message || "Transition impossible.");
    }
  };

  const closeOperation = async (operation) => {
    if (!window.confirm(`Clôturer ${operation.reference} ?`)) return;
    try {
      await apiRequest(`/operations/${operation.id}/close`, { method: "POST" });
      await loadData();
    } catch (err) {
      setError(err.message || "Clôture impossible.");
    }
  };

  const viewHistory = async (operation) => {
    try {
      setError("");
      const entries = await apiRequest(`/operations/${operation.id}/history`);
      setHistory({ operation, entries: entries || [] });
    } catch (err) {
      setError(err.message || "Historique indisponible.");
    }
  };

  return (
    <>
      <div className="page-heading">
        <span className="eyebrow">OPÉRATIONS</span>
        <h1>Gestion des opérations</h1>
        <p className="text-secondary">Pilotage complet des import/export, lignes de produits et expéditions.</p>
      </div>

      <div className="row g-4">
        {canEditOperations && <div className="col-12 col-xl-5">
          <section className="card h-100">
            <div className="card-body">
              <h2>{form.id ? "Modifier l’opération" : "Créer une opération"}</h2>
              <form onSubmit={submit} className="mt-3 management-form">
                <div className="mb-3">
                  <label className="form-label">Type</label>
                  <select
                    className="form-select"
                    value={form.type}
                    onChange={(e) => setForm({ ...form, type: e.target.value })}
                  >
                    <option value="IMPORT">IMPORT</option>
                    <option value="EXPORT">EXPORT</option>
                  </select>
                </div>

                <div className="row g-3">
                  {form.type === "IMPORT" ? (
                    <div className="col-12">
                      <label className="form-label">Fournisseur</label>
                      <select className="form-select" value={form.supplierId} onChange={(e) => setForm({ ...form, supplierId: e.target.value })}>
                        <option value="">Choisir un fournisseur</option>
                        {suppliers.map((item) => (
                          <option key={item.id} value={item.id}>{item.companyName}</option>
                        ))}
                      </select>
                    </div>
                  ) : (
                    <div className="col-12">
                      <label className="form-label">Client</label>
                      <select className="form-select" value={form.customerId} onChange={(e) => setForm({ ...form, customerId: e.target.value })}>
                        <option value="">Choisir un client</option>
                        {customers.map((item) => (
                          <option key={item.id} value={item.id}>{item.companyName}</option>
                        ))}
                      </select>
                    </div>
                  )}

                  <div className="col-md-6">
                    <label className="form-label">Destination</label>
                    <input className="form-control" value={form.destination} onChange={(e) => setForm({ ...form, destination: e.target.value })} required />
                  </div>
                  <div className="col-md-6">
                    <label className="form-label">Transporteur</label>
                    <input className="form-control" value={form.carrier || ""} onChange={(e) => setForm({ ...form, carrier: e.target.value })} />
                  </div>
                  <div className="col-md-6">
                    <label className="form-label">Date commande</label>
                    <input className="form-control" type="date" value={form.orderDate || ""} onChange={(e) => setForm({ ...form, orderDate: e.target.value })} />
                  </div>
                  <div className="col-md-6">
                    <label className="form-label">Date prévue</label>
                    <input className="form-control" type="date" value={form.expectedDate || ""} onChange={(e) => setForm({ ...form, expectedDate: e.target.value })} />
                  </div>
                  <div className="col-md-6">
                    <label className="form-label">Coût planifié</label>
                    <input className="form-control" type="number" step="0.01" value={form.plannedCost || ""} onChange={(e) => setForm({ ...form, plannedCost: e.target.value })} />
                  </div>
                  <div className="col-md-6">
                    <label className="form-label">Coût réel</label>
                    <input className="form-control" type="number" step="0.01" value={form.actualCost || ""} onChange={(e) => setForm({ ...form, actualCost: e.target.value })} />
                  </div>
                </div>

                <div className="mt-4">
                  <div className="d-flex justify-content-between align-items-center mb-2">
                    <strong>Lignes produit</strong>
                    <button type="button" className="btn btn-sm btn-outline-primary" onClick={addLine}>+ Ajouter</button>
                  </div>
                  {form.lines.map((line, idx) => (
                    <div className="row g-2 mb-2" key={idx}>
                      <div className="col-md-4">
                        <select className="form-select" required value={line.productId} onChange={(e) => updateLine(idx, "productId", e.target.value)}>
                          <option value="">Produit</option>
                          {products.map((product) => (
                            <option key={product.id} value={product.id}>{product.name}</option>
                          ))}
                        </select>
                      </div>
                      <div className="col-md-3">
                        <input className="form-control" type="number" min="0.01" step="0.01" required value={line.quantity} placeholder="Qté" onChange={(e) => updateLine(idx, "quantity", e.target.value)} />
                      </div>
                      <div className="col-md-3">
                        <input className="form-control" type="number" min="0" step="0.01" value={line.unitPrice} placeholder="PU" onChange={(e) => updateLine(idx, "unitPrice", e.target.value)} />
                      </div>
                      <div className="col-md-2 d-grid">
                        <button type="button" className="btn btn-outline-danger btn-sm" onClick={() => removeLine(idx)}>Retirer</button>
                      </div>
                    </div>
                  ))}
                </div>

                {error && <div className="alert alert-danger mt-3 mb-0">{error}</div>}

                <div className="d-flex gap-2 mt-4">
                  <button className="btn btn-primary" type="submit">{form.id ? "Mettre à jour" : "Créer"}</button>
                  {form.id && <button type="button" className="btn btn-outline-secondary" onClick={() => setForm(emptyForm)}>Annuler</button>}
                </div>
              </form>
            </div>
          </section>
        </div>}

        <div className="col-12 col-xl-7">
          <section className="card h-100">
            <div className="card-body">
              <div className="d-flex justify-content-between align-items-center mb-3">
                <h2>Opérations</h2>
                <span className="small text-secondary">{operations.length} dossiers</span>
              </div>
              <div className="table-responsive">
                <table className="table align-middle mb-0">
                  <thead>
                    <tr>
                      <th>Référence</th>
                      <th>Type</th>
                      <th>Partenaire</th>
                      <th>Statut</th>
                      <th className="text-end">Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {operations.map((operation) => (
                      <tr key={operation.id}>
                        <td className="fw-semibold">{operation.reference}</td>
                        <td>{operation.type}</td>
                        <td>{operation.supplierName || operation.customerName || "—"}</td>
                        <td><span className="badge text-bg-light">{operation.status}</span></td>
                        <td className="text-end">
                          <div className="d-flex justify-content-end gap-2">
                            {canEditOperations && <button className="btn btn-sm btn-outline-primary" onClick={() => editOperation(operation)}>Éditer</button>}
                            <button className="btn btn-sm btn-outline-dark" onClick={() => viewHistory(operation)}>Historique</button>
                            {nextStatuses[operation.type]?.[operation.status] && <button className="btn btn-sm btn-outline-success" onClick={() => advanceStatus(operation)}>Avancer</button>}
                            {canCloseOrDelete && operation.status === (operation.type === "IMPORT" ? "RECUE" : "LIVREE") && <button className="btn btn-sm btn-outline-secondary" onClick={() => closeOperation(operation)}>Clôturer</button>}
                            {canCloseOrDelete && operation.status !== "CLOTUREE" && <button className="btn btn-sm btn-outline-danger" onClick={() => removeOperation(operation.id)}>Supprimer</button>}
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
                {!operations.length && <p className="text-center text-secondary py-4 mb-0">Aucune opération.</p>}
              </div>
            </div>
          </section>
        </div>
      </div>
      {history && <section className="card mt-4"><div className="card-body"><div className="d-flex justify-content-between align-items-center mb-3"><h2>Historique · {history.operation.reference}</h2><button className="btn btn-sm btn-outline-secondary" onClick={() => setHistory(null)}>Fermer</button></div><div className="table-responsive"><table className="table"><thead><tr><th>Date</th><th>Action</th><th>Utilisateur</th><th>Détails</th></tr></thead><tbody>{history.entries.map((entry) => <tr key={entry.id}><td>{entry.createdAt ? new Date(entry.createdAt).toLocaleString("fr-FR") : "—"}</td><td>{entry.action}</td><td>{entry.actorEmail || "—"}</td><td>{entry.details || "—"}</td></tr>)}</tbody></table>{!history.entries.length && <p className="text-secondary mb-0">Aucune action enregistrée.</p>}</div></div></section>}
    </>
  );
}
