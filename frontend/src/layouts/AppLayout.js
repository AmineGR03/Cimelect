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
    to: "/shipments",
    label: "Expéditions",
    icon: "bi-box-seam",
    roles: ["ADMINISTRATEUR", "RESPONSABLE", "AGENT_IMPORT_EXPORT"],
  },
  {
    to: "/partners",
    label: "Partenaires",
    icon: "bi-buildings",
    roles: ["ADMINISTRATEUR", "AGENT_IMPORT_EXPORT"],
  },
];

export default function AppLayout() {
  const { user } = useSelector((state) => state.auth);
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const items = links.filter((link) => link.roles.includes(user.role));
  const signOut = () => dispatch(logout()).finally(() => navigate("/login"));
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
          <div className="d-flex align-items-center gap-2 mb-3">
            <div className="avatar">
              {user.firstName?.[0]}
              {user.lastName?.[0]}
            </div>
            <div className="text-truncate">
              <strong>
                {user.firstName} {user.lastName}
              </strong>
              <small className="d-block text-white-50">
                {user.role.replaceAll("_", " ")}
              </small>
            </div>
          </div>
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
          <span className="ms-auto small text-secondary">
            25 AOÛT 2026 <i className="bi bi-circle-fill text-success ms-2"></i>
          </span>
        </header>
        <main className="container-fluid content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
