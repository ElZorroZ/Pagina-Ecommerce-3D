const API_BASE_URL = "http://localhost:8080";
document.addEventListener("DOMContentLoaded", () => {
  
// Función para refrescar el access token usando el refresh token
async function refreshAccessToken() {
  const refreshToken = localStorage.getItem("refreshToken");
  if (!refreshToken) {
    // No redirige automáticamente, podés agregarlo si querés
    return null;
  }

  try {
    const response = await fetch(`${API_BASE_URL}/api/auth/refresh`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ refreshToken }),
    });

    if (response.ok) {
      const data = await response.json();
      localStorage.setItem("accessToken", data.token);      // ojo que el token viene como "token"
      localStorage.setItem("refreshToken", data.refreshToken);
      return data.token;
    } else {
      return null;
    }
  } catch (err) {
    console.error("Error al refrescar el token", err);
    return null;
  }
}

async function fetchConRefresh(url, options = {}) {
  options.headers = options.headers || {};

  // Agregar Authorization si falta
  if (!options.headers['Authorization']) {
    const token = localStorage.getItem('accessToken');
    if (token) {
      options.headers['Authorization'] = `Bearer ${token}`;
    }
  }

  // Controlar Content-Type solo si body NO es FormData
  if (!(options.body instanceof FormData)) {
    // Si no existe Content-Type, se lo ponemos JSON (o el que quieras)
    if (!options.headers['Content-Type']) {
      options.headers['Content-Type'] = 'application/json';
    }
  } else {
    // Si body es FormData, eliminar cualquier Content-Type para evitar conflictos
    if ('Content-Type' in options.headers) {
      delete options.headers['Content-Type'];
    }
  }

  let response = await fetch(url, options);

  // Si el token expiró o es inválido, intentamos refrescar
  if (response.status === 401) {
    const nuevoToken = await refreshAccessToken();
    if (nuevoToken) {
      // Clonamos las opciones para evitar problemas con body reutilizable
      const newOptions = {
        ...options,
        headers: {
          ...options.headers,
          'Authorization': `Bearer ${nuevoToken}`
        }
      };
      response = await fetch(url, newOptions);
    } else {
      // No se pudo refrescar el token
      throw new Error('No autorizado - token expirado y no se pudo refrescar');
    }
  }

  return response;
}

(() => {

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

    const res = await fetchConRefresh(`${API_BASE_URL}/api/usuario/colaboradores`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(colaboradorPayload),
    });

    if (!res.ok) {
      const errorText = await res.text();
      throw new Error(errorText || "Error al alternar permiso.");
    }

    alert("Permiso alternado correctamente.");
    form.reset();
    await cargarColaboradores(); // actualiza la lista
  } catch (error) {
    alert("Error: " + error.message);
  }
});

})();

});
