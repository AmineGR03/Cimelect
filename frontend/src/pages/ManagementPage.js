import { useCallback, useEffect, useMemo, useState } from "react";
import { apiRequest } from "../services/api";

const emptyForm = {
  id: null,
  companyName: "",
  country: "",
  contactName: "",
  email: "",
  phone: "",
};

export default function ManagementPage({ type }) {
  const [items, setItems] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const endpoint = useMemo(() => {
    if (type === "suppliers") return "/suppliers";
    if (type === "customers") return "/customers";
    if (type === "products") return "/products";
    return "/suppliers";
  }, [type]);

  const label = useMemo(() => {
    if (type === "suppliers") return "Fournisseur";
    if (type === "customers") return "Client";
    if (type === "products") return "Produit";
    return "Élément";
  }, [type]);

  const loadAll = useCallback(async () => {
    try {
      setLoading(true);
      const data = await apiRequest(endpoint);
      setItems(data || []);
    } catch (err) {
      setError(err.message || "Chargement impossible.");
    } finally {
      setLoading(false);
    }
  }, [endpoint]);

  useEffect(() => {
    setForm(emptyForm);
    loadAll();
  }, [loadAll]);

  const submit = async (event) => {
    event.preventDefault();
    setError("");
    try {
      const payload =
        type === "products"
          ? {
              sku: form.sku || "",
              name: form.companyName,
              description: form.contactName || "",
              unit: form.country || "",
            }
          : {
              companyName: form.companyName,
              country: form.country,
              contactName: form.contactName,
              email: form.email,
              phone: form.phone,
            };

      if (form.id) {
        await apiRequest(`${endpoint}/${form.id}`, {
          method: "PUT",
          body: JSON.stringify(payload),
        });
      } else {
        await apiRequest(endpoint, {
          method: "POST",
          body: JSON.stringify(payload),
        });
      }

      setForm(emptyForm);
      await loadAll();
    } catch (err) {
      setError(err.message || "Sauvegarde impossible.");
    }
  };

  const remove = async (id) => {
    if (!window.confirm(`Supprimer ce ${label.toLowerCase()} ?`)) return;
    try {
      await apiRequest(`${endpoint}/${id}`, { method: "DELETE" });
      await loadAll();
    } catch (err) {
      setError(err.message || "Suppression impossible.");
    }
  };

  const edit = (item) => {
    if (type === "products") {
      setForm({
        id: item.id,
        companyName: item.name,
        country: item.unit,
        contactName: item.description,
        email: "",
        phone: "",
        sku: item.sku,
      });
      return;
    }
    setForm({
      id: item.id,
      companyName: item.companyName,
      country: item.country,
      contactName: item.contactName,
      email: item.email,
      phone: item.phone,
    });
  };

  return (
    <>
      <div className="page-heading">
        <span className="eyebrow">GESTION</span>
        <h1>{type === "suppliers" ? "Fournisseurs" : type === "customers" ? "Clients" : "Produits"}</h1>
        <p className="text-secondary">Créez, modifiez et supprimez les éléments du réseau opérationnel.</p>
      </div>

      <div className="row g-4">
        <div className="col-12 col-xl-4">
          <section className="card h-100">
            <div className="card-body">
              <h2>{form.id ? `Modifier ${label}` : `Nouveau ${label}`}</h2>
              <form onSubmit={submit} className="mt-3 management-form">
                {type === "products" ? (
                  <>
                    <div className="mb-3">
                      <label className="form-label">SKU</label>
                      <input className="form-control" value={form.sku || ""} onChange={(e) => setForm({ ...form, sku: e.target.value })} />
                    </div>
                    <div className="mb-3">
                      <label className="form-label">Nom</label>
                      <input className="form-control" value={form.companyName} onChange={(e) => setForm({ ...form, companyName: e.target.value })} required />
                    </div>
                    <div className="mb-3">
                      <label className="form-label">Description</label>
                      <input className="form-control" value={form.contactName || ""} onChange={(e) => setForm({ ...form, contactName: e.target.value })} />
                    </div>
                    <div className="mb-3">
                      <label className="form-label">Unité</label>
                      <input className="form-control" value={form.country || ""} onChange={(e) => setForm({ ...form, country: e.target.value })} />
                    </div>
                  </>
                ) : (
                  <>
                    <div className="mb-3">
                      <label className="form-label">Nom de l’entreprise</label>
                      <input className="form-control" value={form.companyName} onChange={(e) => setForm({ ...form, companyName: e.target.value })} required />
                    </div>
                    <div className="mb-3">
                      <label className="form-label">Pays</label>
                      <input className="form-control" value={form.country} onChange={(e) => setForm({ ...form, country: e.target.value })} required />
                    </div>
                    <div className="mb-3">
                      <label className="form-label">Contact</label>
                      <input className="form-control" value={form.contactName || ""} onChange={(e) => setForm({ ...form, contactName: e.target.value })} />
                    </div>
                    <div className="mb-3">
                      <label className="form-label">Email</label>
                      <input className="form-control" type="email" value={form.email || ""} onChange={(e) => setForm({ ...form, email: e.target.value })} />
                    </div>
                    <div className="mb-3">
                      <label className="form-label">Téléphone</label>
                      <input className="form-control" value={form.phone || ""} onChange={(e) => setForm({ ...form, phone: e.target.value })} />
                    </div>
                  </>
                )}

                {error && <div className="alert alert-danger mt-2 mb-0">{error}</div>}

                <div className="d-flex gap-2 mt-3">
                  <button className="btn btn-primary" type="submit" disabled={loading}>
                    {loading ? "Enregistrement..." : form.id ? "Mettre à jour" : "Créer"}
                  </button>
                  {form.id && (
                    <button type="button" className="btn btn-outline-secondary" onClick={() => setForm(emptyForm)}>
                      Annuler
                    </button>
                  )}
                </div>
              </form>
            </div>
          </section>
        </div>

        <div className="col-12 col-xl-8">
          <section className="card h-100">
            <div className="card-body">
              <div className="d-flex justify-content-between align-items-center mb-3">
                <h2>{type === "suppliers" ? "Fournisseurs" : type === "customers" ? "Clients" : "Produits"}</h2>
                <span className="small text-secondary">{items.length} éléments</span>
              </div>
              <div className="table-responsive">
                <table className="table align-middle mb-0">
                  <thead>
                    <tr>
                      {type === "products" ? <th>Produit</th> : <th>Entreprise</th>}
                      {type === "products" ? <th>SKU</th> : <th>Pays</th>}
                      {type === "products" ? <th>Unité</th> : <th>Contact</th>}
                      <th className="text-end">Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {items.map((item) => (
                      <tr key={item.id}>
                        <td>{type === "products" ? item.name : item.companyName}</td>
                        <td>{type === "products" ? item.sku : item.country}</td>
                        <td>{type === "products" ? item.unit : item.contactName || "—"}</td>
                        <td className="text-end">
                          <div className="d-flex justify-content-end gap-2">
                            <button className="btn btn-sm btn-outline-primary" onClick={() => edit(item)}>Éditer</button>
                            <button className="btn btn-sm btn-outline-danger" onClick={() => remove(item.id)}>Supprimer</button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
                {!items.length && <p className="text-center text-secondary py-4 mb-0">Aucun élément trouvé.</p>}
              </div>
            </div>
          </section>
        </div>
      </div>
    </>
  );
}
