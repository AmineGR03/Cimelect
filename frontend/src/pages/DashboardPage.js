import { useSelector } from "react-redux";
import KpiCard from "../components/KpiCard";
import StatusBadge from "../components/StatusBadge";

export default function DashboardPage() {
  const { data, loading } = useSelector((state) => state.dashboard);
  const dashboard = data.dashboard || {};
  const operations = data.operations || [];
  const alerts = dashboard.activeAiAlerts || [];
  const active = operations.filter((item) => item.status !== "CLOTUREE").length;
  const trend = dashboard.volumeTrend || [];
  return (
    <>
      <div className="page-heading d-flex justify-content-between align-items-end">
        <div>
          <span className="eyebrow">CENTRE DE CONTRÔLE</span>
          <h1>Vue d’ensemble</h1>
          <p className="text-secondary mb-0">
            La situation de vos flux, en un coup d’œil.
          </p>
        </div>
        <span className="system-status">
          <i className="bi bi-circle-fill"></i> SYSTÈME OPÉRATIONNEL
        </span>
      </div>
      {loading && (
        <div className="progress mb-3" role="progressbar">
          <div className="progress-bar" style={{ width: "45%" }}></div>
        </div>
      )}
      <div className="row g-3 mb-4">
        <div className="col-12 col-sm-6 col-xl-3">
          <KpiCard
            label="Opérations actives"
            value={active}
            caption="Import & export en cours"
            icon="↗"
            tone="warning"
          />
        </div>
        <div className="col-12 col-sm-6 col-xl-3">
          <KpiCard
            label="Expéditions suivies"
            value={(data.shipments || []).length}
            caption="Dossiers actuellement ouverts"
            icon="▣"
          />
        </div>
        <div className="col-12 col-sm-6 col-xl-3">
          <KpiCard
            label="Délai moyen"
            value={
              dashboard.averageDelayDays
                ? `${dashboard.averageDelayDays} j`
                : "—"
            }
            caption="Sur les opérations clôturées"
            icon="◷"
            tone="success"
          />
        </div>
        <div className="col-12 col-sm-6 col-xl-3">
          <KpiCard
            label="Taux d’anomalie"
            value={dashboard.anomalyRate ? `${dashboard.anomalyRate}%` : "—"}
            caption="Analyse intelligente"
            icon="!"
            tone="danger"
          />
        </div>
      </div>
      <div className="row g-3">
        <div className="col-12 col-xl-8">
          <section className="card chart-card h-100">
            <div className="card-body">
              <div className="d-flex justify-content-between align-items-start">
                <div>
                  <span className="eyebrow">VOLUME DES FLUX</span>
                  <h2>Activité récente</h2>
                </div>
                <div className="small text-secondary">
                  <i className="bi bi-square-fill text-primary me-1"></i>{" "}
                  Imports{" "}
                  <i className="bi bi-square-fill text-warning ms-2 me-1"></i>{" "}
                  Exports
                </div>
              </div>
              <div className="chart-bars">
                {trend.slice(-7).map((item) => (
                  <div className="bar-group" key={item.period}>
                    <div className="bars">
                      <span
                        className="bar import"
                        style={{
                          height: `${Math.min(100, item.importCount * 15 + 8)}%`,
                        }}
                      ></span>
                      <span
                        className="bar export"
                        style={{
                          height: `${Math.min(100, item.exportCount * 15 + 8)}%`,
                        }}
                      ></span>
                    </div>
                    <small>{item.period}</small>
                  </div>
                ))}
              </div>
              {!trend.length && (
                <p className="text-secondary text-center py-5 mb-0">
                  Les tendances apparaîtront dès les premières opérations.
                </p>
              )}
            </div>
          </section>
        </div>
        <div className="col-12 col-xl-4">
          <section className="card h-100">
            <div className="card-body">
              <div className="d-flex justify-content-between">
                <div>
                  <span className="eyebrow">VIGILANCE</span>
                  <h2>Alertes IA</h2>
                </div>
                <span className="badge text-bg-danger">{alerts.length}</span>
              </div>
              {alerts.length ? (
                alerts.map((alert) => (
                  <div className="alert-line" key={alert.shipmentId}>
                    <span>!</span>
                    <div>
                      <strong>Expédition #{alert.shipmentId}</strong>
                      <p>{alert.message}</p>
                    </div>
                  </div>
                ))
              ) : (
                <div className="empty-state">
                  <i className="bi bi-check2-circle"></i>
                  <p>
                    Aucune anomalie active.
                    <small>Vos flux avancent normalement.</small>
                  </p>
                </div>
              )}
            </div>
          </section>
        </div>
      </div>
      <section className="card mt-3">
        <div className="card-body">
          <div className="d-flex justify-content-between align-items-center mb-3">
            <h2>Dernières opérations</h2>
            <span className="small text-secondary">
              {operations.length} dossiers
            </span>
          </div>
          <div className="table-responsive">
            <table className="table align-middle mb-0">
              <thead>
                <tr>
                  <th>Référence</th>
                  <th>Type</th>
                  <th>Partenaire</th>
                  <th>Échéance</th>
                  <th>Statut</th>
                </tr>
              </thead>
              <tbody>
                {operations.slice(0, 5).map((item) => (
                  <tr key={item.id}>
                    <td className="fw-semibold">
                      {item.reference || `OP-${item.id}`}
                    </td>
                    <td>{item.type}</td>
                    <td>{item.supplierName || item.customerName || "—"}</td>
                    <td>{item.expectedDate || "—"}</td>
                    <td>
                      <StatusBadge status={item.status} />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </section>
    </>
  );
}
