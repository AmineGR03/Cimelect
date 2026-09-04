import { useEffect, useState } from "react";
import { apiRequest } from "../services/api";

const emptyForm = {
  id: null,
  firstName: "",
  lastName: "",
  email: "",
  password: "",
  role: "AGENT_IMPORT_EXPORT",
  enabled: true,
};

export default function UsersPage() {
  const [users, setUsers] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);

  const loadUsers = async () => {
    try {
      const data = await apiRequest("/users");
      setUsers(data || []);
    } catch (err) {
      setError(err.message || "Impossible de charger les utilisateurs.");
    }
  };

  useEffect(() => {
    loadUsers();
  }, []);

  const submit = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError("");

    try {
      const payload = {
        firstName: form.firstName,
        lastName: form.lastName,
        email: form.email,
        password: form.password || undefined,
        role: form.role,
        enabled: form.enabled,
      };

      if (form.id) {
        await apiRequest(`/users/${form.id}`, {
          method: "PUT",
          body: JSON.stringify(payload),
        });
      } else {
        await apiRequest("/users", {
          method: "POST",
          body: JSON.stringify(payload),
        });
      }

      setForm(emptyForm);
      await loadUsers();
    } catch (err) {
      setError(err.message || "Erreur lors de la sauvegarde.");
    } finally {
      setSaving(false);
    }
  };

  const remove = async (id) => {
    if (!window.confirm("Supprimer ce compte ?")) return;
    try {
      await apiRequest(`/users/${id}`, { method: "DELETE" });
      await loadUsers();
    } catch (err) {
      setError(err.message || "Suppression impossible.");
    }
  };

  const edit = (user) => {
    setForm({
      id: user.id,
      firstName: user.firstName,
      lastName: user.lastName,
      email: user.email,
      password: "",
      role: user.role,
      enabled: user.enabled,
    });
  };

  return (
    <>
      <div className="page-heading">
        <span className="eyebrow">ADMINISTRATION</span>
        <h1>Utilisateurs</h1>
        <p className="text-secondary">Gérez les comptes, les rôles et l’état des accès.</p>
      </div>

      <div className="row g-4">
        <div className="col-12 col-xl-4">
          <section className="card h-100">
            <div className="card-body">
              <h2>{form.id ? "Modifier le compte" : "Créer un compte"}</h2>
              <form onSubmit={submit} className="mt-3 management-form">
                <div className="row g-3">
                  <div className="col-md-6">
                    <label className="form-label">Prénom</label>
                    <input
                      className="form-control"
                      value={form.firstName}
                      onChange={(e) => setForm({ ...form, firstName: e.target.value })}
                      required
                    />
                  </div>
                  <div className="col-md-6">
                    <label className="form-label">Nom</label>
                    <input
                      className="form-control"
                      value={form.lastName}
                      onChange={(e) => setForm({ ...form, lastName: e.target.value })}
                      required
                    />
                  </div>
                  <div className="col-12">
                    <label className="form-label">Email</label>
                    <input
                      className="form-control"
                      type="email"
                      value={form.email}
                      onChange={(e) => setForm({ ...form, email: e.target.value })}
                      required
                    />
                  </div>
                  <div className="col-12">
                    <label className="form-label">Mot de passe {form.id ? "(laisser vide pour conserver)" : ""}</label>
                    <input
                      className="form-control"
                      type="password"
                      value={form.password}
                      onChange={(e) => setForm({ ...form, password: e.target.value })}
                      placeholder={form.id ? "Nouveau mot de passe" : "Mot de passe"}
                      {...(!form.id && { required: true })}
                    />
                  </div>
                  <div className="col-md-6">
                    <label className="form-label">Rôle</label>
                    <select
                      className="form-select"
                      value={form.role}
                      onChange={(e) => setForm({ ...form, role: e.target.value })}
                    >
                      <option value="ADMINISTRATEUR">ADMINISTRATEUR</option>
                      <option value="RESPONSABLE">RESPONSABLE</option>
                      <option value="AGENT_IMPORT_EXPORT">AGENT_IMPORT_EXPORT</option>
                    </select>
                  </div>
                  <div className="col-md-6">
                    <label className="form-label">Statut</label>
                    <select
                      className="form-select"
                      value={form.enabled ? "active" : "inactive"}
                      onChange={(e) => setForm({ ...form, enabled: e.target.value === "active" })}
                    >
                      <option value="active">Actif</option>
                      <option value="inactive">Inactif</option>
                    </select>
                  </div>
                </div>

                {error && <div className="alert alert-danger mt-3 mb-0">{error}</div>}

                <div className="d-flex gap-2 mt-4">
                  <button className="btn btn-primary" type="submit" disabled={saving}>
                    {saving ? "Enregistrement..." : form.id ? "Mettre à jour" : "Créer"}
                  </button>
                  {form.id && (
                    <button
                      type="button"
                      className="btn btn-outline-secondary"
                      onClick={() => setForm(emptyForm)}
                    >
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
                <h2>Comptes</h2>
                <span className="small text-secondary">{users.length} comptes</span>
              </div>
              <div className="table-responsive">
                <table className="table align-middle mb-0">
                  <thead>
                    <tr>
                      <th>Nom</th>
                      <th>Email</th>
                      <th>Rôle</th>
                      <th>État</th>
                      <th className="text-end">Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {users.map((user) => (
                      <tr key={user.id}>
                        <td>{user.firstName} {user.lastName}</td>
                        <td>{user.email}</td>
                        <td><span className="badge text-bg-light">{user.role}</span></td>
                        <td>
                          <span className={`badge ${user.enabled ? "text-bg-success" : "text-bg-secondary"}`}>
                            {user.enabled ? "Actif" : "Inactif"}
                          </span>
                        </td>
                        <td className="text-end">
                          <div className="d-flex justify-content-end gap-2">
                            <button className="btn btn-sm btn-outline-primary" onClick={() => edit(user)}>Éditer</button>
                            <button className="btn btn-sm btn-outline-danger" onClick={() => remove(user.id)}>Supprimer</button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
                {!users.length && <p className="text-center text-secondary py-4 mb-0">Aucun utilisateur trouvé.</p>}
              </div>
            </div>
          </section>
        </div>
      </div>
    </>
  );
}
