// Función para refrescar token (la dejé igual)
function base64UrlToBase64(base64url) {
  return base64url.replace(/-/g, '+').replace(/_/g, '/');
}

function fixBase64Padding(base64) {
  const padLength = (4 - (base64.length % 4)) % 4;
  return base64 + "=".repeat(padLength);
}

function base64ToUint8Array(base64) {
  base64 = base64UrlToBase64(base64).replace(/\s/g, '');
  base64 = fixBase64Padding(base64);
  const raw = atob(base64);
  const uint8Array = new Uint8Array(raw.length);
  for (let i = 0; i < raw.length; i++) {
    uint8Array[i] = raw.charCodeAt(i);
  }
  return uint8Array;
}


function mostrarArchivoComprimido(base64, nombre = 'archivo.zip') {
  if (!base64) return;

  const byteCharacters = atob(base64);
  const byteNumbers = new Array(byteCharacters.length);
  for (let i = 0; i < byteCharacters.length; i++) {
    byteNumbers[i] = byteCharacters.charCodeAt(i);
  }
  const byteArray = new Uint8Array(byteNumbers);

  const blob = new Blob([byteArray], { type: 'application/octet-stream' });
  const url = URL.createObjectURL(blob);

  const preview = document.getElementById('comprimido-preview');
  preview.innerHTML = '';

  const link = document.createElement('a');
  link.href = url;
  link.download = nombre;
  link.textContent = `Descargar ${nombre}`;
  link.style.display = 'inline-block';
  link.style.marginRight = '10px';
  preview.appendChild(link);
}
document.addEventListener("DOMContentLoaded", () => {
  const tablaPedidosBody = document.getElementById("tabla-pedidos");
  const filtroEstado = document.getElementById("filtro-estado");
  let pedidosCache = []; // Guardar pedidos originales
  const categoriesDropdown = document.querySelector("#categories-dropdown .dropdown-content");
  const shopTrigger = document.getElementById("shop-trigger");
  async function cargarPedidos() {
  try {
    mostrarCarga("Cargando pedidos..."); // Mostrar overlay
    const response = await authManager.fetchWithAuth(`${API_BASE_URL}/api/pedido/verPedidos`);
    if (!response.ok) throw new Error("Error al obtener los pedidos");

    pedidosCache = await response.json();
    renderPedidos(pedidosCache);
  } catch (error) {
    console.error("Error al cargar pedidos:", error.message);
    mostrarError("No se pudieron cargar los pedidos"); // usando mensajes.js
  } finally {
        ocultarCarga(); // Ocultar overlay siempre
  }
}

  function renderPedidos(pedidos) {
  tablaPedidosBody.innerHTML = "";

  const estadoFiltro = filtroEstado.value;

  pedidos
    .filter(p => estadoFiltro === "TODOS" || p.estado === estadoFiltro)
    .forEach(pedido => {
      const productos = pedido.productos ?? [];
      const tieneDigital = productos.some(prod => prod.esDigital);
      const formato = tieneDigital ? "Digital" : "Físico";

      const fila = document.createElement("tr");

      // Estado
      let estadoHTML;
      if (tieneDigital) {
        estadoHTML = `<span>${pedido.estado}</span>`;
      } else {
        const estados = ["PROCESANDO", "PAGADO", "FALLIDO", "CANCELADO"];
        const opciones = estados.map(e => `<option value="${e}" ${pedido.estado === e ? "selected" : ""}>${e}</option>`).join("");
        estadoHTML = `<select class="estado-select">${opciones}</select>
                      <button class="guardar-estado">Guardar</button>`;
      }

      // Botón seleccionar solo si hay producto físico y no está seleccionado
      const mostrarBotonSeleccionar = !tieneDigital && window.pedidoSeleccionadoId !== pedido.id;
      const botonSeleccionar = mostrarBotonSeleccionar ? `<button class="btn-seleccionar">Seleccionar</button>` : "";

      fila.innerHTML = `
        <td>${pedido.id}</td>
        <td>${new Date(pedido.fechaPedido).toLocaleDateString()}</td>
        <td>${estadoHTML}</td>
        <td>${formato}</td>
        <td>$${pedido.total.toFixed(2)}</td>
        <td>${botonSeleccionar}</td>
      `;

      if (!tieneDigital) {
        // Evento Guardar estado
        fila.querySelector(".guardar-estado")?.addEventListener("click", async () => {
          const nuevoEstado = fila.querySelector(".estado-select").value;
          try {
            mostrarCarga("Cambiando estado..."); // Mostrar overlay
            const res = await authManager.fetchWithAuth(
              `${API_BASE_URL}/api/pedido/CambiarEstado?estado=${encodeURIComponent(nuevoEstado)}&id=${pedido.id}`,
              { method: "PUT" }
            );
            if (!res.ok) throw new Error("No se pudo actualizar el estado");
            mostrarExito("Estado actualizado correctamente");
            cargarPedidos(); // recargar tabla para reflejar cambios
          } catch (err) {
            console.error(err);
            mostrarError("Error al actualizar el estado");
          } finally {
              ocultarCarga(); // Ocultar overlay siempre
          }
        });

        // Evento Seleccionar pedido
        fila.querySelector(".btn-seleccionar")?.addEventListener("click", () => {
          window.pedidoSeleccionadoId = pedido.id;
          renderPedidos(pedidos); // recargar tabla para ocultar botón
          selectPedido(pedido.id);
          mostrarExito(`Pedido ${pedido.id} seleccionado.`);
        });
      }

      tablaPedidosBody.appendChild(fila);
    });
}

  // Filtrar cuando cambie el select
  filtroEstado.addEventListener("change", () => {
    renderPedidos(pedidosCache);
  });

  cargarPedidos();

 // Seleccionar pedido y cargar en formulario + preview
async function selectPedido(pedidoId) {
  try {
    mostrarCarga("Cargando pedido..."); // Mostrar overlay
    // --- Obtener pedido usando authManager ---
    const res = await authManager.fetchWithAuth(
      `${API_BASE_URL}/api/pedido/verPedido?id=${pedidoId}`
    );

    if (!res.ok) throw new Error("No se pudo cargar el pedido");

    const pedido = await res.json();
    console.log("PedidoUsuarioDTO recibido:", pedido);

    // --- Guardar pedido seleccionado globalmente para UI ---
    window.pedidoSeleccionadoId = pedidoId;

    // --- Cargar datos del cliente en formulario ---
    cargarPedidoClienteEnFormulario(pedido);

    // --- Inicializar contenedor de productos ---
    window.productoState = window.productoState || {};
    window.productoState.productos = pedido.productos || [];

    // --- Renderizar todos los productos del pedido ---
    cargarProductosDelPedido(pedido.productos || []);

    mostrarExito(`Pedido ${pedidoId} cargado correctamente`);

  } catch (error) {
    console.error("Error al cargar pedido:", error);
    mostrarError("Error al cargar el pedido");
  }finally {
        ocultarCarga(); // Ocultar overlay siempre
    }
}

// Cargar solo los datos del cliente
function cargarPedidoClienteEnFormulario(pedido) {
  document.getElementById("pedido-id").value = pedido.id || "";
  document.getElementById("nombre").value = `${pedido.nombre || ""} ${pedido.apellido || ""}`;
  document.getElementById("direccion").value = pedido.direccion || "";
  document.getElementById("ciudad").value = pedido.ciudad || "";
  document.getElementById("codigo-postal").value = pedido.cp || "";
  document.getElementById("telefono").value = pedido.telefono || "";
  document.getElementById("email").value = pedido.gmail || "";
}
// Renderiza todos los productos en el contenedor
function cargarProductosDelPedido(productos) {
  const contenedor = document.getElementById("productos-container");
  const contenedorComprimidos = document.getElementById("comprimido-preview");
  contenedor.innerHTML = "";
  contenedorComprimidos.innerHTML = ""; // limpiar antes de renderizar

  productos.forEach((prod, index) => {
    const productoDiv = document.createElement("div");
    productoDiv.className = "producto-item";
    productoDiv.style.border = "1px solid #ccc";
    productoDiv.style.padding = "10px";
    productoDiv.style.marginBottom = "10px";
    productoDiv.style.borderRadius = "5px";

    const colorText = prod.esDigital ? "DIGITAL" : (prod.colorNombre || "-");

    productoDiv.innerHTML = `
      <p><strong>Producto ${index + 1}</strong></p>
      <p>Nombre: ${prod.nombre || "-"}</p>
      <p>Color: ${colorText}</p>
      <p>Cantidad: ${prod.cantidad || "-"}</p>
    `;

    contenedor.appendChild(productoDiv);

    // Solo mostrar ZIP si NO es digital
    if (prod.archivoBase64 && !prod.esDigital) {
      const enlace = document.createElement("a");
      enlace.href = `data:application/zip;base64,${prod.archivoBase64}`;
      enlace.download = `${prod.nombre || 'archivo'}.zip`;
      enlace.textContent = `${prod.nombre || 'archivo'}.zip`;
      enlace.style.display = "block";
      contenedorComprimidos.appendChild(enlace);
    }
  });
}


async function cargarCategoriasYSeleccionar(categoriaIdSeleccionada) {
  try {
    const res = await authManager.fetchWithAuth(`${API_BASE_URL}/api/categoria/combo`);
    if (!res.ok) throw new Error("No se pudieron cargar las categorías");

    const categorias = await res.json();
    // Actualizar dropdown con links
    renderCategories(categorias);

  } catch (err) {
    console.error("Error cargando categorías:", err);
    mostrarError("Error cargando categorías: " + err.message);
  }
}

cargarCategoriasYSeleccionar();
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


  // Inicializar dropdown del shop
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
  initializeDropdown();

  });
