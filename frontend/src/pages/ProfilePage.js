import { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { apiRequest } from "../services/api";
import { setUser } from "../store/authSlice";

export default function ProfilePage() {
  const user = useSelector((state) => state.auth.user);
  const dispatch = useDispatch();
  const [form, setForm] = useState({ firstName: "", lastName: "", password: "" });
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    apiRequest("/users/me")
      .then((data) => setForm({ firstName: data.firstName || "", lastName: data.lastName || "", password: "" }))
      .catch((err) => setError(err.message || "Profil indisponible."));
  }, []);

  const submit = async (event) => {
    event.preventDefault();
    setMessage("");
    setError("");
    try {
      const data = await apiRequest("/users/me", {
        method: "PUT",
        body: JSON.stringify({ firstName: form.firstName, lastName: form.lastName, password: form.password || null }),
      });
      dispatch(setUser(data));
      setForm({ firstName: data.firstName || "", lastName: data.lastName || "", password: "" });
      setMessage("Profil mis à jour.");
    } catch (err) {
      setError(err.message || "Mise à jour impossible.");
    }
  };

  return (
    <>
      <div className="page-heading"><span className="eyebrow">COMPTE</span><h1>Mon profil</h1><p className="text-secondary">Mettez à jour vos informations personnelles.</p></div>
      <div className="row"><div className="col-12 col-lg-6"><section className="card"><div className="card-body"><p className="text-secondary">{user?.email}</p><form onSubmit={submit} className="management-form"><label className="form-label">Prénom</label><input className="form-control mb-3" required value={form.firstName} onChange={(e) => setForm({ ...form, firstName: e.target.value })} /><label className="form-label">Nom</label><input className="form-control mb-3" required value={form.lastName} onChange={(e) => setForm({ ...form, lastName: e.target.value })} /><label className="form-label">Nouveau mot de passe</label><input className="form-control" type="password" minLength="8" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} /><small className="text-secondary d-block mt-1">Laissez vide pour conserver le mot de passe actuel.</small>{message && <div className="alert alert-success mt-3">{message}</div>}{error && <div className="alert alert-danger mt-3">{error}</div>}<button className="btn btn-primary mt-3">Enregistrer</button></form></div></section></div></div>
    </>
  );
}
