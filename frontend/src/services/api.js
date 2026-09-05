const API_URL = process.env.REACT_APP_API_URL || "/api";

export async function apiRequest(path, options = {}) {
  const token = localStorage.getItem("cimelect_token");
  const isFormData = options.body instanceof FormData;
  let response;
  try {
    response = await fetch(`${API_URL}${path}`, {
      ...options,
      headers: {
        ...(options.body && !isFormData
          ? { "Content-Type": "application/json" }
          : {}),
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...options.headers,
      },
    });
  } catch (error) {
    throw new Error(
      "Backend inaccessible. Vérifiez que Spring Boot est démarré sur le port 8080.",
    );
  }
  if (!response.ok) {
    if (response.status === 401) throw new Error("Session expirée. Reconnectez-vous.");
    if (response.status === 403) throw new Error("Accès refusé pour ce rôle.");
    if (response.status >= 500) throw new Error("Le serveur rencontre une erreur.");
    throw new Error("Requête refusée par le serveur.");
  }
  return response.status === 204 ? null : response.json();
}
