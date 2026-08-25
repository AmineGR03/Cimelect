import { useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useNavigate } from "react-router-dom";
import { login } from "../store/authSlice";

export default function LoginPage() {
  const [form, setForm] = useState({ email: "", password: "" });
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { loading, error } = useSelector((state) => state.auth);
  const submit = (event) => {
    event.preventDefault();
    dispatch(login(form))
      .unwrap()
      .then(() => navigate("/dashboard"));
  };
  return (
    <main className="login-page">
      <section className="login-panel">
        <div className="brand-symbol mb-4">
          C<span>/</span>
        </div>
        <p className="eyebrow">CIMELECT / LOGISTICS OS</p>
        <h1>
          Le commerce,
          <br />
          <em>en mouvement.</em>
        </h1>
        <p className="login-copy">
          Pilotez vos flux internationaux avec une vision nette de chaque
          opération.
        </p>
        <form onSubmit={submit} className="mt-4">
          <label className="form-label">
            Email professionnel
            <input
              className="form-control"
              type="email"
              required
              value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })}
              placeholder="nom@entreprise.com"
            />
          </label>
          <label className="form-label mt-3">
            Mot de passe
            <input
              className="form-control"
              type="password"
              required
              value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
              placeholder="Votre mot de passe"
            />
          </label>
          {error && <div className="alert alert-danger py-2 mt-3">{error}</div>}
          <button className="btn btn-primary w-100 mt-3" disabled={loading}>
            {loading ? "Connexion..." : "Ouvrir la session"}{" "}
            <i className="bi bi-arrow-right ms-2"></i>
          </button>
        </form>
        <small className="text-secondary d-block mt-4">
          Connexion sécurisée par authentification JWT
        </small>
      </section>
      <aside className="login-art">
        <div className="route-line"></div>
        <div>
          <span className="eyebrow text-white-50">01 / CONTROL TOWER</span>
          <strong>
            MOVE
            <br />
            WITH
            <br />
            CLARITY
          </strong>
          <p>Imports · Exports · Expéditions</p>
        </div>
      </aside>
    </main>
  );
}
