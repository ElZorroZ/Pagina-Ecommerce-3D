<<<<<<< HEAD
=======
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
    const response = await fetch("http://localhost:8080/api/auth/refresh", {
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

>>>>>>> parent of 391f6a9 (Merge branch 'main' of https://github.com/ElZorroZ/Pagina-Ecommerce-3D)
document.addEventListener("DOMContentLoaded", () => {
  const tablaBody = document.getElementById("tabla-productos");
  const categoriesDropdown = document.querySelector("#categories-dropdown .dropdown-content");
  const shopTrigger = document.getElementById("shop-trigger");
  async function cargarColaboradores() {
    try {
<<<<<<< HEAD
      const response = await authManager.fetchWithAuth(
        `${API_BASE_URL}/api/usuario/colaboradores`
      );
=======
      const response = await fetchConRefresh("http://localhost:8080/api/usuario/colaboradores");
>>>>>>> parent of 391f6a9 (Merge branch 'main' of https://github.com/ElZorroZ/Pagina-Ecommerce-3D)
      if (!response.ok) throw new Error("Error al obtener los colaboradores");

      const colaboradores = await response.json();
      tablaBody.innerHTML = "";

      colaboradores.forEach((colaborador) => {
        const fila = document.createElement("tr");

        fila.innerHTML = `
          <td>${colaborador.id}</td>
          <td>${colaborador.nombre}</td>
          <td>${colaborador.gmail}</td>
          <td>
            ${
              colaborador.id === 1
                ? ""
                : `<button class="eliminar">Eliminar</button>`
            }
          </td>
        `;

        if (colaborador.id !== 1) {
          fila
            .querySelector(".eliminar")
            .addEventListener("click", () =>
              eliminarColaborador(colaborador.gmail)
            );
        }

        tablaBody.appendChild(fila);
      });
    } catch (error) {
      console.error("Error al cargar colaboradores:", error.message);
      mostrarError("❌ No se pudieron cargar los colaboradores");
    }
  }

  window.cargarColaboradores = cargarColaboradores;

  cargarColaboradores();

  async function eliminarColaborador(gmail) {
    // Mostrar confirmación estilo modal
    mostrarConfirmacion("¿Seguro que querés quitar este colaborador?", async (confirmado) => {
        if (!confirmado) return;

<<<<<<< HEAD
        try {
            const res = await authManager.fetchWithAuth(
                `${API_BASE_URL}/api/usuario/colaboradores`,
                {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ gmail }),
                }
            );

            if (!res.ok) {
                let errorMessage = "Error al eliminar colaborador.";
                try {
                    const text = await res.text();
                    const data = JSON.parse(text);
                    if (data.message) errorMessage = data.message;
                } catch {
                    // Mantener el mensaje por defecto
                }
                throw new Error(errorMessage);
            }

            mostrarExito("Colaborador removido correctamente");
            cargarColaboradores(); // refresca la tabla
        } catch (error) {
            mostrarError("Error: " + error.message);
        }
    });
}


  // --- Carga de categorías ---
  async function loadCategories() {
    const categories = await API.getCategories();
    renderCategories(categories);
  }

  function renderCategories(categorias) {
    if (!Array.isArray(categorias)) return;
    categoriesDropdown.innerHTML = "";

    categorias.forEach(cat => {
      const link = document.createElement("a");
      link.href = "#";
      link.className = "dropdown-category";
      link.textContent = cat.nombre;
      link.dataset.categoryId = cat.id;

      // 🔑 Redirección al hacer click
      link.addEventListener("click", (e) => {
        e.preventDefault();
        window.location.href = `/categoria.html?id=${cat.id}`;
=======
      const res = await fetch("http://localhost:8080/api/usuario/colaboradores", {
        method: "POST",
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json"
        },
        body: JSON.stringify({ gmail: gmail })
>>>>>>> parent of 391f6a9 (Merge branch 'main' of https://github.com/ElZorroZ/Pagina-Ecommerce-3D)
      });

      categoriesDropdown.appendChild(link);
    });
  }


  function initializeDropdown() {
    if (!shopTrigger) return;
    const categoriesDropdownMenu = document.getElementById("categories-dropdown");

    shopTrigger.addEventListener("mouseenter", () => {
      categoriesDropdownMenu.classList.add("show");
    });

    const navDropdown = shopTrigger.parentElement;
    navDropdown.addEventListener("mouseleave", () => {
      categoriesDropdownMenu.classList.remove("show");
    });
  }

  loadCategories();
  initializeDropdown();
});
