import { useCallback, useEffect, useState } from "react";
import { apiRequest } from "../services/api";

const documentTypes = ["FACTURE", "PACKING_LIST", "CERTIFICAT", "TRANSPORT", "AUTRE"];
const apiUrl = process.env.REACT_APP_API_URL || "/api";

export default function DocumentsPage() {
  const [operations, setOperations] = useState([]);
  const [operationId, setOperationId] = useState("");
  const [documents, setDocuments] = useState([]);
  const [requirements, setRequirements] = useState([]);
  const [type, setType] = useState("FACTURE");
  const [file, setFile] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const loadOperations = async () => {
    const [imports, exports] = await Promise.all([
      apiRequest("/operations/import"),
      apiRequest("/operations/export"),
    ]);
    setOperations([...(imports || []), ...(exports || [])]);
  };

  const loadDocuments = useCallback(async (id) => {
    if (!id) {
      setDocuments([]);
      setRequirements([]);
      return;
    }
    const operation = operations.find((item) => String(item.id) === String(id));
    const [documentList, requirementList] = await Promise.all([
      apiRequest(`/operations/${id}/documents`),
      apiRequest(`/document-requirements?type=${operation.type}`),
    ]);
    setDocuments(documentList || []);
    setRequirements(requirementList || []);
  }, [operations]);

  useEffect(() => {
    loadOperations().catch((err) => setError(err.message || "Chargement impossible."));
  }, []);

  useEffect(() => {
    loadDocuments(operationId).catch((err) => setError(err.message || "Chargement impossible."));
  }, [loadDocuments, operationId]);

  const upload = async (event) => {
    event.preventDefault();
    if (!operationId || !file) return;
    setError("");
    try {
      setLoading(true);
      const body = new FormData();
      body.append("type", type);
      body.append("file", file);
      await apiRequest(`/operations/${operationId}/documents`, { method: "POST", body });
      setFile(null);
      event.target.reset();
      await loadDocuments(operationId);
    } catch (err) {
      setError(err.message || "Envoi impossible.");
    } finally {
      setLoading(false);
    }
  };

  const remove = async (id) => {
    if (!window.confirm("Supprimer ce document ?")) return;
    try {
      await apiRequest(`/documents/${id}?confirmed=true`, { method: "DELETE" });
      await loadDocuments(operationId);
    } catch (err) {
      setError(err.message || "Suppression impossible.");
    }
  };

  const download = async (document) => {
    try {
      const response = await fetch(`${apiUrl}/documents/${document.id}/download`, {
        headers: { Authorization: `Bearer ${localStorage.getItem("cimelect_token")}` },
      });
      if (!response.ok) throw new Error("Téléchargement impossible.");
      const blob = await response.blob();
      const url = URL.createObjectURL(blob);
      const anchor = window.document.createElement("a");
      anchor.href = url;
      anchor.download = document.originalFilename || "document";
      anchor.click();
      URL.revokeObjectURL(url);
    } catch (err) {
      setError(err.message || "Téléchargement impossible.");
    }
  };

  const selectedOperation = operations.find((item) => String(item.id) === String(operationId));
  const presentTypes = new Set(documents.map((document) => document.type));

  return (
    <>
      <div className="page-heading"><span className="eyebrow">CONFORMITÉ</span><h1>Documents</h1><p className="text-secondary">Déposez et contrôlez les documents rattachés à chaque opération.</p></div>
      <div className="row g-4">
        <div className="col-12 col-xl-5"><section className="card h-100"><div className="card-body">
          <h2>Opération</h2>
          <select className="form-select mt-3" value={operationId} onChange={(e) => setOperationId(e.target.value)}><option value="">Choisir une opération</option>{operations.map((operation) => <option key={operation.id} value={operation.id}>{operation.reference} · {operation.type}</option>)}</select>
          {selectedOperation && <><h3 className="mt-4">Documents requis</h3>{requirements.map((requirement) => <div className="metric-row" key={requirement.id || requirement.documentType}><span>{requirement.documentType}</span><strong className={requirement.required && !presentTypes.has(requirement.documentType) ? "text-danger" : "text-success"}>{requirement.required && !presentTypes.has(requirement.documentType) ? "Manquant" : "Présent"}</strong></div>)}</>}
          <hr className="my-4" /><h2>Ajouter un document</h2>
          <form onSubmit={upload} className="management-form mt-3"><label className="form-label">Type</label><select className="form-select mb-3" value={type} onChange={(e) => setType(e.target.value)}>{documentTypes.map((item) => <option key={item}>{item}</option>)}</select><label className="form-label">Fichier</label><input className="form-control" type="file" required onChange={(e) => setFile(e.target.files[0] || null)} /><button className="btn btn-primary mt-3" disabled={!operationId || loading}>{loading ? "Envoi..." : "Déposer le document"}</button></form>
        </div></section></div>
        <div className="col-12 col-xl-7"><section className="card h-100"><div className="card-body"><div className="d-flex justify-content-between mb-3"><h2>Documents déposés</h2><span className="small text-secondary">{documents.length} fichiers</span></div>{error && <div className="alert alert-danger">{error}</div>}<div className="table-responsive"><table className="table align-middle"><thead><tr><th>Nom</th><th>Type</th><th className="text-end">Actions</th></tr></thead><tbody>{documents.map((document) => <tr key={document.id}><td>{document.originalFilename}</td><td>{document.type}</td><td className="text-end"><button className="btn btn-sm btn-outline-primary me-2" onClick={() => download(document)}>Télécharger</button><button className="btn btn-sm btn-outline-danger" onClick={() => remove(document.id)}>Supprimer</button></td></tr>)}</tbody></table>{!operationId && <p className="text-center text-secondary py-4 mb-0">Sélectionnez une opération.</p>}{operationId && !documents.length && <p className="text-center text-secondary py-4 mb-0">Aucun document déposé.</p>}</div></div></section></div>
      </div>
    </>
  );
}
