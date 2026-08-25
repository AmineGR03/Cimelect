const API_URL = process.env.REACT_APP_API_URL || "/api";

export async function apiRequest(path, options = {}) {
  const token = localStorage.getItem("cimelect_token");
  let response;
  try {
    response = await fetch(`${API_URL}${path}`, {
      ...options,
      headers: {
        ...(options.body ? { "Content-Type": "application/json" } : {}),
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...options.headers,
      },
    });
  } catch (error) {
    throw new Error(
      "Backend inaccessible. Vérifiez que Spring Boot est démarré sur le port 8080.",
    );
  }
  if (!response.ok)
    throw new Error(
      response.status === 401
        ? "Session expirée."
        : "Impossible de joindre le serveur.",
    );
  return response.status === 204 ? null : response.json();
}
