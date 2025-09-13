document.addEventListener("DOMContentLoaded", () => {
<<<<<<< HEAD
  (() => {
    // Estado global
    window.colaboradorState = window.colaboradorState || {};

    const form = document.getElementById("form-producto");
=======
// Función para refrescar el access token usando el refresh token
async function refreshAccessToken() {
  const refreshToken = localStorage.getItem("refreshToken");
  if (!refreshToken) {
    // No redirige automáticamente, podés agregarlo si querés
    return null;
  }

  try {
    const response = await fetch("http://localhost:8080/api/auth/refresh", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ refreshToken }),
    });
>>>>>>> parent of 391f6a9 (Merge branch 'main' of https://github.com/ElZorroZ/Pagina-Ecommerce-3D)

    if (!form) return;

    form.addEventListener("submit", async (e) => {
      e.preventDefault();

      const gmail = document.getElementById("email").value.trim();

      if (!gmail) {
        mostrarError("Por favor completa el email.");
        return;
      }

      try {
        const colaboradorPayload = { gmail };

        const res = await authManager.fetchWithAuth(
          `${API_BASE_URL}/api/usuario/colaboradores`,
          {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(colaboradorPayload),
          }
        );

        if (!res.ok) {
          const text = await res.text();
          console.error("❌ Error al alternar permiso, status:", res.status, "body:", text);
          let errorMessage = "Error al alternar permiso.";
          try {
            const errData = JSON.parse(text);
            if (errData.message) errorMessage = errData.message;
          } catch {}
          throw new Error(errorMessage);
        }



<<<<<<< HEAD
        mostrarExito("Permiso alternado correctamente.");
        form.reset();
        await cargarColaboradores(); // actualiza la lista
      } catch (error) {
        mostrarError("Error: " + error.message);
      }
=======
  // Estado global
  window.colaboradorState = window.colaboradorState || {};

  const form = document.getElementById("form-producto");

  form.addEventListener("submit", async (e) => {
  e.preventDefault();

  const gmail = document.getElementById("email").value.trim();

  if (!gmail) {
    alert("Por favor completa el email.");
    return;
  }

  try {
    const colaboradorPayload = { gmail };

    const res = await fetchConRefresh("http://localhost:8080/api/usuario/colaboradores", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(colaboradorPayload),
>>>>>>> parent of 391f6a9 (Merge branch 'main' of https://github.com/ElZorroZ/Pagina-Ecommerce-3D)
    });
  })();
});
