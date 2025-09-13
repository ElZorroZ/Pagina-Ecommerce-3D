<<<<<<< HEAD
=======
// Estado global para colores y archivos
window.productoState = window.productoState || {
  archivosSeleccionados: []
};
let preview;
  // Preview archivos
  function actualizarPreview() {
    const preview = document.getElementById("preview-imagenes");
    preview.innerHTML = "";

    const archivos = window.categoriaState.archivosSeleccionados;
    if (!archivos || archivos.length === 0) return;

    archivos.forEach((archivo, idx) => {
        const div = document.createElement("div");
        div.style.position = "relative";
        div.style.display = "inline-block";
        div.style.marginRight = "10px";

        const img = document.createElement("img");
        img.style.width = "80px";
        img.style.height = "80px";
        img.style.objectFit = "cover";
        img.style.border = "1px solid #ccc";
        img.style.borderRadius = "4px";

        if (archivo instanceof File) {
        const reader = new FileReader();
        reader.onload = (e) => {
            img.src = e.target.result;
        };
        reader.readAsDataURL(archivo);
        } else {
        img.src = archivo.linkArchivo || archivo.url || "ruta_default.jpg";
        }

        const btnEliminar = document.createElement("button");
        btnEliminar.textContent = "X";
        btnEliminar.style.position = "absolute";
        btnEliminar.style.top = "0";
        btnEliminar.style.right = "0";
        btnEliminar.style.background = "rgba(255,0,0,0.7)";
        btnEliminar.style.color = "white";
        btnEliminar.style.border = "none";
        btnEliminar.style.cursor = "pointer";
        btnEliminar.style.borderRadius = "0 4px 0 4px";
        btnEliminar.style.padding = "0 4px";
        btnEliminar.title = "Eliminar imagen";

        btnEliminar.addEventListener("click", () => {
        if (confirm("¿Seguro que querés eliminar esta imagen?")) {
            archivos.splice(idx, 1);
            actualizarPreview();
        }
        });

        div.appendChild(img);
        div.appendChild(btnEliminar);
        preview.appendChild(div);
    });
    }

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
  const btnEditar = document.getElementById("btn-editar-producto");
  const categoriesDropdown = document.querySelector("#categories-dropdown .dropdown-content");
  const shopTrigger = document.getElementById("shop-trigger");

 let categoriaSeleccionadaId = null; // ID actualmente seleccionado
async function cargarCategorias() {
  try {
    const res = await authManager.fetchWithAuth(`${API_BASE_URL}/api/categoria`);
    if (!res.ok) throw new Error("Error al obtener las categorías");
    const categorias = await res.json();

    // Actualizar tabla
    tablaBody.innerHTML = "";
    categorias.forEach(categoria => {
      const fila = document.createElement("tr");
      fila.innerHTML = `
        <td>${categoria.id}</td>
        <td>${categoria.nombre}</td>
        <td>
          <button class="select">Seleccionar</button>
          <button class="eliminar">Eliminar</button>
        </td>
      `;

      const btnSelect = fila.querySelector(".select");

      // Ocultar si es la categoría seleccionada
      if (categoria.id === categoriaSeleccionadaId) {
        btnSelect.style.display = "none";
        mostrarExito(`Categoria "${categoria.nombre}" seleccionada.`);
      } else {
        btnSelect.style.display = "inline-block";
      }

      btnSelect.addEventListener("click", () => selectCategoria(categoria.id));

      fila.querySelector(".eliminar").addEventListener("click", () => eliminarCategoria(categoria.id));
      tablaBody.appendChild(fila);
    });

<<<<<<< HEAD
    // Actualizar dropdown
    renderCategories(categorias);
=======
  // Cargar productos y llenar tabla
  async function cargarCategorias() {
    try {
      const response = await fetchConRefresh("http://localhost:8080/api/categoria");
      if (!response.ok) throw new Error("Error al obtener las categorías");
      const categorias = await response.json();
      tablaBody.innerHTML = "";
      tablaBody.innerHTML = "";
>>>>>>> parent of 391f6a9 (Merge branch 'main' of https://github.com/ElZorroZ/Pagina-Ecommerce-3D)

  } catch (error) {
    console.error("Error al cargar categorías:", error);
    mostrarError("No se pudieron cargar las categorías");
  }
}

async function selectCategoria(categoriaId) {
  try {
    const res = await authManager.fetchWithAuth(`${API_BASE_URL}/api/categoria/${categoriaId}`);
    if (!res.ok) throw new Error("No se pudo cargar la categoría");
    const data = await res.json();

    document.getElementById("categoria-id").value = data.id || "";
    document.getElementById("nombre").value = data.nombre || "";
    btnEditar.style.display = "block";
    localStorage.setItem("categoriaId", categoriaId);

    // Actualizar categoría seleccionada y recargar la tabla
    categoriaSeleccionadaId = categoriaId;
    cargarCategorias();

  } catch (error) {
    console.error("Error al cargar categoría:", error);
    mostrarError("Error al cargar la categoría");
  }
}

  window.cargarCategorias = cargarCategorias;
  cargarCategorias();

<<<<<<< HEAD
  // Render categories in dropdown
  // Render categories in dropdown
function renderCategories(categorias) {
  if (!Array.isArray(categorias)) return;
  categoriesDropdown.innerHTML = "";
=======
    async function toggleCategoriaDestacada(categoriaId) {
        try {
            const token = localStorage.getItem("accessToken");
            const res = await fetch(`http://localhost:8080/api/categoria/toggleCategoriaDestacada/${categoriaId}`, {
            method: "POST",
            headers: {
                "Authorization": `Bearer ${token}`
            }
            });
>>>>>>> parent of 391f6a9 (Merge branch 'main' of https://github.com/ElZorroZ/Pagina-Ecommerce-3D)

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
    });

    categoriesDropdown.appendChild(link);
  });
}


  // Actualizar categoría
async function actualizarCategoria() {
  const id = document.getElementById("categoria-id").value;
  const nombre = document.getElementById("nombre").value.trim();
  if (!nombre) { 
    mostrarError("Completa el nombre"); 
    return; 
  }

  const payload = { nombre };
  const formData = new FormData();
  formData.append("categoria", new Blob([JSON.stringify(payload)], { type: "application/json" }));

  try {
    const res = await authManager.fetchWithAuth(`${API_BASE_URL}/api/categoria/${id}`, {
      method: "PUT",
      body: formData
    });

    if (!res.ok) {
      const error = await res.text();
      mostrarError("Error al actualizar categoría: " + error);
      return;
    }

<<<<<<< HEAD
    mostrarExito("Categoría actualizada correctamente");
    cargarCategorias();
  } catch (error) {
    mostrarError("Error al actualizar categoría: " + error.message);
  }
}
=======
    async function selectCategoria(categoriaId) {
        try {
            const token = localStorage.getItem("accessToken");
            const res = await fetch(`http://localhost:8080/api/categoria/${categoriaId}`, {
            headers: { "Authorization": `Bearer ${token}` }
            });

            if (!res.ok) throw new Error("No se pudo cargar la categoría");

            const data = await res.json();
            console.log("CategoriaDTO recibida:", data);

            // Cargar datos en el formulario
            document.getElementById("categoria-id").value = data.id || "";
            document.getElementById("nombre").value = data.nombre || "";
            document.getElementById("descripcion").value = data.descripcion || "";

            // Mostrar imagen si existe
            const preview = document.getElementById("preview-imagenes");
            if (data.linkArchivo && preview) {
            window.categoriaState.archivosSeleccionados = [{
                linkArchivo: data.linkArchivo
            }];
            actualizarPreview(); // usa tu función actual para mostrar la imagen
            } else {
            window.categoriaState.archivosSeleccionados = [];
            actualizarPreview();
            }

            // Mostrar si es destacada
            const destacadoLabel = document.getElementById("categoria-destacada-label");
            if (destacadoLabel) {
            destacadoLabel.textContent = data.destacada ? "⭐ Destacada" : "☆ No destacada";
            }

            // Habilitar botón editar
            btnEditar.style.display = "block";
            localStorage.setItem("categoriaId", categoriaId);
        } catch (error) {
            console.error("Error al cargar categoría:", error);
            alert("Error al cargar la categoría");
        }
    }

  async function actualizarCategoria() {
        const id = document.getElementById("categoria-id").value;
        const nombre = document.getElementById("nombre").value.trim();
        const descripcion = document.getElementById("descripcion").value.trim();

        const archivosExistentes = window.categoriaState.archivosSeleccionados
            .filter(a => !(a instanceof File))
            .map(a => ({ linkArchivo: a.linkArchivo }));

        const archivosNuevos = window.categoriaState.archivosSeleccionados
            .filter(a => a instanceof File);

        const categoriaPayload = {
            nombre,
            descripcion,
            archivos: archivosExistentes
        };

        const formData = new FormData();
        formData.append("categoria", new Blob([JSON.stringify(categoriaPayload)], { type: "application/json" }));

        if (archivosNuevos.length > 0) {
        formData.append("file", archivosNuevos[0]); // solo el primer archivo
        }


        const token = localStorage.getItem("accessToken");

        const res = await fetch(`http://localhost:8080/api/categoria/${id}`, {
            method: "PUT",
            headers: {
                "Authorization": `Bearer ${token}`
            },
            body: formData
        });

        if (!res.ok) {
            const error = await res.text();
            alert("Error al actualizar categoría: " + error);
            return;
        }

        alert("Categoría actualizada correctamente");
        cargarCategorias();
    }



>>>>>>> parent of 391f6a9 (Merge branch 'main' of https://github.com/ElZorroZ/Pagina-Ecommerce-3D)

  if (btnEditar) {
    btnEditar.addEventListener("click", e => {
      e.preventDefault();
      actualizarCategoria();
    });
  }

<<<<<<< HEAD
  // Eliminar categoría
async function eliminarCategoria(id) {
  mostrarConfirmacion("¿Seguro que querés eliminar esta categoría?", async (confirmado) => {
    if (!confirmado) return;

    try {
      const res = await authManager.fetchWithAuth(`${API_BASE_URL}/api/categoria/${id}`, {
        method: "DELETE"
      });
      if (!res.ok) throw new Error("Error al eliminar categoría");
      mostrarExito("Categoría eliminada correctamente");
      cargarCategorias();
    } catch (error) {
      mostrarError("Error: " + error.message);
=======
    async function eliminarCategoria(id) {
        if (!confirm("¿Seguro que querés eliminar esta categoría?")) return;
        try {
            const token = localStorage.getItem("accessToken");
            const res = await fetch(`http://localhost:8080/api/categoria/${id}`, {
            method: "DELETE",
            headers: {
                "Authorization": `Bearer ${token}`
            }
            });
            if (!res.ok) throw new Error("Error al eliminar categoría");
            alert("Categoría eliminada correctamente");
            cargarCategorias(); // refrescar la tabla de categorías
        } catch (error) {
            alert("Error: " + error.message);
        }
>>>>>>> parent of 391f6a9 (Merge branch 'main' of https://github.com/ElZorroZ/Pagina-Ecommerce-3D)
    }
  });
}


  // Inicializar dropdown del shop
  function initializeDropdown() {
  if (!shopTrigger) return;
  const categoriesDropdownMenu = document.getElementById("categories-dropdown");

  // Mostrar/ocultar al pasar el mouse
  shopTrigger.addEventListener("mouseenter", () => {
    categoriesDropdownMenu.classList.add("show");
  });

  // Ocultar al salir del dropdown
  const navDropdown = shopTrigger.parentElement;
  navDropdown.addEventListener("mouseleave", () => {
    categoriesDropdownMenu.classList.remove("show");
  });
}

  initializeDropdown();
});
