document.addEventListener("DOMContentLoaded", () => {
  const tablaBody = document.getElementById("tabla-productos");
  const categoriesDropdown = document.querySelector("#categories-dropdown .dropdown-content");
  const shopTrigger = document.getElementById("shop-trigger");
  async function cargarColaboradores() {
    try {
      mostrarCarga("Cargando colaboradores..."); // Mostrar overlay
      const response = await authManager.fetchWithAuth(
        `${API_BASE_URL}/api/usuario/colaboradores`
      );
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
    }finally {
        ocultarCarga(); // Ocultar overlay siempre
    }
  }

  window.cargarColaboradores = cargarColaboradores;

  cargarColaboradores();

  async function eliminarColaborador(gmail) {
    // Mostrar confirmación estilo modal
    mostrarConfirmacion("¿Seguro que querés quitar este colaborador?", async (confirmado) => {
        if (!confirmado) return;

        try {
            mostrarCarga("Eliminando colaborador..."); // Mostrar overlay
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
        }finally {
        ocultarCarga(); // Ocultar overlay siempre
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
