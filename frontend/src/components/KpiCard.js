export default function KpiCard({
  label,
  value,
  caption,
  icon,
  tone = "primary",
}) {
  return (
    <div className="card kpi-card h-100">
      <div className="card-body d-flex justify-content-between">
        <div>
          <span className="kpi-label">{label}</span>
          <h2 className="kpi-value">{value}</h2>
          <small className="text-secondary">{caption}</small>
        </div>
        <span className={`kpi-icon bg-${tone}-subtle text-${tone}`}>
          {icon}
        </span>
      </div>
    </div>
  );
}
