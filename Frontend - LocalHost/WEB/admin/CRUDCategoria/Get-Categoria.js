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

    // Actualizar dropdown
    renderCategories(categorias);

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

  // Render categories in dropdown
  // Render categories in dropdown
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

    mostrarExito("Categoría actualizada correctamente");
    cargarCategorias();
  } catch (error) {
    mostrarError("Error al actualizar categoría: " + error.message);
  }
}

  if (btnEditar) {
    btnEditar.addEventListener("click", e => {
      e.preventDefault();
      actualizarCategoria();
    });
  }

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
