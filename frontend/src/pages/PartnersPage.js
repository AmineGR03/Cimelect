import { useSelector } from "react-redux";
export default function PartnersPage() {
  const partners = useSelector((state) => state.dashboard.data.partners || []);
  return (
    <>
      <div className="page-heading">
        <span className="eyebrow">ÉCOSYSTÈME</span>
        <h1>Partenaires</h1>
        <p className="text-secondary">
          Clients et fournisseurs connectés à vos flux.
        </p>
      </div>
      <div className="row g-3">
        {partners.map((item) => (
          <div
            className="col-12 col-md-6 col-xl-4"
            key={`${item.kind}-${item.id}`}
          >
            <article className="card partner-card h-100">
              <div className="card-body d-flex align-items-start gap-3">
                <div className="partner-avatar">
                  {item.companyName?.slice(0, 2).toUpperCase()}
                </div>
                <div>
                  <span className="eyebrow">{item.kind}</span>
                  <h3>{item.companyName}</h3>
                  <p className="text-secondary mb-1">
                    {item.country || "Pays non renseigné"}
                  </p>
                  <small>{item.contactName || "Contact non renseigné"}</small>
                </div>
              </div>
            </article>
          </div>
        ))}
        {!partners.length && (
          <p className="text-secondary">Aucun partenaire trouvé.</p>
        )}
      </div>
    </>
  );
}
