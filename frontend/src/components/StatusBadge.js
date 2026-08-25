const labels = {
  CREEE: "Créée",
  EN_TRANSIT: "En transit",
  DEDOUANEMENT: "Dédouanement",
  RECUE: "Reçue",
  PREPARATION: "Préparation",
  EXPEDIEE: "Expédiée",
  LIVREE: "Livrée",
  CLOTUREE: "Clôturée",
  EN_PREPARATION: "En préparation",
  ARRIVEE: "Arrivée",
};
export default function StatusBadge({ status }) {
  return (
    <span className={`badge rounded-pill status-${status?.toLowerCase()}`}>
      {labels[status] || status || "Non défini"}
    </span>
  );
}
