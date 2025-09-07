// Estado global para colores y archivos
window.colaboradorState = window.colaboradorState || {
  archivosSeleccionados: []
};

// Función para refrescar token (la dejé igual)
async function refreshAccessToken() {
  const refreshToken = localStorage.getItem("refreshToken");
  if (!refreshToken) {
    console.warn("No hay refresh token guardado");
    return null;
  }
  try {
    const response = await fetch("https://forma-programada.onrender.com/api/auth/refresh", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ refreshToken }),
    });
    if (response.ok) {
      const data = await response.json();
      if (!data.token) console.warn("No se recibió token");
      if (!data.refreshToken) console.warn("No se recibió refreshToken");
      localStorage.setItem("accessToken", data.token);
      localStorage.setItem("refreshToken", data.refreshToken);
      return data.token;
    } else {
      let errorBody = await response.text();
      try { errorBody = JSON.parse(errorBody).message || errorBody; } catch {}
      console.warn("Refresh token inválido o expirado", response.status, errorBody);
      return null;
    }
  } catch (err) {
    console.error("Error al refrescar el token", err);
    return null;
  }
}

let refreshInProgress = false;

async function fetchConRefresh(url, options = {}) {
  options.headers = options.headers || {};
  if (!options.headers['Authorization']) {
    const token = localStorage.getItem('accessToken');
    if (token) options.headers['Authorization'] = `Bearer ${token}`;
  }

  let response = await fetch(url, options);

  if (response.status === 401 && !refreshInProgress) {
    refreshInProgress = true;
    const nuevoToken = await refreshAccessToken();
    refreshInProgress = false;
    if (nuevoToken) {
      options.headers['Authorization'] = `Bearer ${nuevoToken}`;
      response = await fetch(url, options);
    } else {
      throw new Error('No autorizado - token expirado y no se pudo refrescar');
    }
  }

  return response;
}

document.addEventListener("DOMContentLoaded", () => {
  const tablaBody = document.getElementById("tabla-productos");

  async function cargarColaboradores() {
    try {
      const response = await fetchConRefresh("https://forma-programada.onrender.com/api/usuario/colaboradores");
      if (!response.ok) throw new Error("Error al obtener los colaboradores");

      const colaboradores = await response.json();
      tablaBody.innerHTML = "";

      colaboradores.forEach(colaborador => {
        const fila = document.createElement("tr");

        fila.innerHTML = `
          <td>${colaborador.id}</td>
          <td>${colaborador.nombre}</td>
          <td>${colaborador.gmail}</td>
          <td>
            ${
              colaborador.id === 1
                ? ''
                : `<button class="eliminar">Eliminar</button>`
            }
          </td>
        `;

        if (colaborador.id !== 1) {
          // Le pasás directamente el email
          fila.querySelector(".eliminar").addEventListener("click", () => eliminarColaborador(colaborador.id, colaborador.gmail));
        }

        tablaBody.appendChild(fila);
      });
    } catch (error) {
      console.error("Error al cargar colaboradores:", error.message);
      alert("No se pudieron cargar los colaboradores");
    }
  }


  window.cargarColaboradores = cargarColaboradores;


  cargarColaboradores();

  async function eliminarColaborador(id, gmail) {
    if (!confirm("¿Seguro que querés quitar este colaborador?")) return;
    try {
      const token = localStorage.getItem("accessToken");

      const res = await fetch("https://forma-programada.onrender.com/api/usuario/colaboradores", {
        method: "POST",
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json"
        },
        body: JSON.stringify({ gmail: gmail })
      });

      if (!res.ok) throw new Error(await res.text());

      alert("Colaborador removido correctamente");
      cargarColaboradores(); // refresca la tabla
    } catch (error) {
      alert("Error: " + error.message);
    }
  }


});
