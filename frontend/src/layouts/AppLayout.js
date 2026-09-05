import { useEffect, useState } from "react";
import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import { logout } from "../store/authSlice";

const links = [
  {
    to: "/dashboard",
    label: "Vue d’ensemble",
    icon: "bi-grid-1x2",
    roles: ["ADMINISTRATEUR", "RESPONSABLE"],
  },
  {
    to: "/operations",
    label: "Opérations",
    icon: "bi-arrow-left-right",
    roles: ["ADMINISTRATEUR", "RESPONSABLE", "AGENT_IMPORT_EXPORT"],
  },
  {
    to: "/operations/manage",
    label: "Gestion opérations",
    icon: "bi-clipboard-data",
    roles: ["ADMINISTRATEUR", "RESPONSABLE", "AGENT_IMPORT_EXPORT"],
  },
  {
    to: "/shipments",
    label: "Expéditions",
    icon: "bi-box-seam",
    roles: ["ADMINISTRATEUR", "RESPONSABLE", "AGENT_IMPORT_EXPORT"],
  },
  {
    to: "/shipments/manage",
    label: "Gestion expéditions",
    icon: "bi-boxes",
    roles: ["ADMINISTRATEUR", "AGENT_IMPORT_EXPORT"],
  },
  {
    to: "/documents",
    label: "Documents",
    icon: "bi-file-earmark-text",
    roles: ["ADMINISTRATEUR", "RESPONSABLE", "AGENT_IMPORT_EXPORT"],
  },
  {
    to: "/partners",
    label: "Partenaires",
    icon: "bi-buildings",
    roles: ["ADMINISTRATEUR", "AGENT_IMPORT_EXPORT"],
  },
  {
    to: "/partners/suppliers",
    label: "Fournisseurs",
    icon: "bi-truck",
    roles: ["ADMINISTRATEUR", "AGENT_IMPORT_EXPORT"],
  },
  {
    to: "/partners/customers",
    label: "Clients",
    icon: "bi-person-badge",
    roles: ["ADMINISTRATEUR", "AGENT_IMPORT_EXPORT"],
  },
  {
    to: "/products",
    label: "Produits",
    icon: "bi-box",
    roles: ["ADMINISTRATEUR", "AGENT_IMPORT_EXPORT"],
  },
  {
    to: "/users",
    label: "Utilisateurs",
    icon: "bi-people",
    roles: ["ADMINISTRATEUR"],
  },
  {
    to: "/audit",
    label: "Audit",
    icon: "bi-clock-history",
    roles: ["ADMINISTRATEUR", "RESPONSABLE"],
  },
  {
    to: "/profile",
    label: "Mon profil",
    icon: "bi-person-circle",
    roles: ["ADMINISTRATEUR", "RESPONSABLE", "AGENT_IMPORT_EXPORT"],
  },
];

export default function AppLayout() {
  const { user } = useSelector((state) => state.auth);
  const { loading, error } = useSelector((state) => state.dashboard);
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const [currentDate, setCurrentDate] = useState(() => new Date());
  const items = links.filter((link) => link.roles.includes(user.role));
  const signOut = () => dispatch(logout()).finally(() => navigate("/login"));

  useEffect(() => {
    const timer = window.setInterval(() => setCurrentDate(new Date()), 60000);
    return () => window.clearInterval(timer);
  }, []);

  const formattedDate = new Intl.DateTimeFormat("fr-FR", {
    day: "2-digit",
    month: "long",
    year: "numeric",
  }).format(currentDate).toUpperCase();
  const systemLabel = error ? "CONNEXION À VÉRIFIER" : loading ? "SYNCHRONISATION" : "SYSTÈME OPÉRATIONNEL";
  const systemTone = error ? "text-danger" : loading ? "text-warning" : "text-success";
  return (
    <div className="app-shell">
      <aside className="sidebar offcanvas-lg offcanvas-start" id="mainSidebar">
        <div className="brand">
          <span className="brand-symbol">
            C<span>/</span>
          </span>
          <span>
            <strong>CIMELECT</strong>
            <small>TRADE OPERATIONS</small>
          </span>
        </div>
        <nav className="nav flex-column">
          {items.map((link) => (
            <NavLink className="nav-link" to={link.to} key={link.to}>
              <i className={`bi ${link.icon}`}></i>
              {link.label}
            </NavLink>
          ))}
        </nav>
        <div className="sidebar-footer">
          <button
            className="btn btn-outline-light btn-sm w-100"
            onClick={signOut}
          >
            <i className="bi bi-box-arrow-right me-2"></i>Déconnexion
          </button>
        </div>
      </aside>
      <div className="page-area">
        <header className="topbar">
          <button
            className="btn d-lg-none"
            data-bs-toggle="offcanvas"
            data-bs-target="#mainSidebar"
          >
            <i className="bi bi-list fs-4"></i>
          </button>
          <span className="breadcrumb-text">
            CIMELECT <b>/</b> espace de travail
          </span>
          <div className="ms-auto topbar-meta">
            <div className="topbar-user">
              <div className="avatar">
                {user.firstName?.[0]}
                {user.lastName?.[0]}
              </div>
              <div className="text-truncate">
                <strong>
                  {user.firstName} {user.lastName}
                </strong>
                <small>{user.role.replaceAll("_", " ")}</small>
              </div>
            </div>
            <span className="small text-secondary topbar-system">
              {formattedDate} <span className="ms-2">{systemLabel}</span> <i className={`bi bi-circle-fill ${systemTone} ms-2`}></i>
            </span>
          </div>
        </header>
        <main className="container-fluid content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
