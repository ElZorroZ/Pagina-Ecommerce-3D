// Función para refrescar token (la dejé igual)
const API_BASE_URL = "https://forma-programada.onrender.com";

async function refreshAccessToken() {
  const refreshToken = localStorage.getItem("refreshToken");
  if (!refreshToken) {
    console.warn("No hay refresh token guardado");
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

  async function cargarPedidos() {
    try {
      const response = await fetchConRefresh(`${API_BASE_URL}/api/pedido/verPedidos`);
      if (!response.ok) throw new Error("Error al obtener los pedidos");

      pedidosCache = await response.json();
      renderPedidos(pedidosCache);
    } catch (error) {
      console.error("Error al cargar pedidos:", error.message);
      alert("No se pudieron cargar los pedidos");
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
          const estados = ["PROCESANDO", "PAGADO", "REEMBOLSADO", "FALLIDO", "CANCELADO"];
          const opciones = estados.map(e => `<option value="${e}" ${pedido.estado === e ? "selected" : ""}>${e}</option>`).join("");
          estadoHTML = `<select class="estado-select">${opciones}</select>
                        <button class="guardar-estado">Guardar</button>`;
        }

        // Botón seleccionar solo si hay producto físico
        const botonSeleccionar = !tieneDigital
          ? `<button class="btn-seleccionar">Seleccionar</button>`
          : "";

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
          fila.querySelector(".guardar-estado").addEventListener("click", async () => {
            const nuevoEstado = fila.querySelector(".estado-select").value;
            try {
              const res = await fetchConRefresh(
                `${API_BASE_URL}/api/pedido/CambiarEstado?estado=${encodeURIComponent(nuevoEstado)}&id=${pedido.id}`,
                { method: "PUT" }
              );
              if (!res.ok) throw new Error("No se pudo actualizar el estado");
              alert("Estado actualizado correctamente");
            } catch (err) {
              console.error(err);
              alert("Error al actualizar el estado");
            }
          });

          // Evento Seleccionar pedido
          fila.querySelector(".btn-seleccionar").addEventListener("click", () => {
            selectPedido(pedido.id);
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
    const token = localStorage.getItem("accessToken");
    const res = await fetch(`${API_BASE_URL}/api/pedido/verPedido?id=${pedidoId}`, {
      headers: { "Authorization": `Bearer ${token}` }
    });
    if (!res.ok) throw new Error("No se pudo cargar el pedido");
    const data = await res.json();
    console.log('PedidoUsuarioDTO recibido:', data);

    // Cargar datos del cliente
    cargarPedidoClienteEnFormulario(data);

    // Cargar primer producto del pedido (si hay varios)
    if (data.productos && data.productos.length > 0) {
      cargarProductoEnFormulario(data.productos[0]);
    } else {
      document.getElementById('comprimido-preview').innerHTML = "";
    }

  } catch (error) {
    console.error(error);
    alert("Error al cargar pedido");
  }
}

// Cargar solo los datos del cliente
function cargarPedidoClienteEnFormulario(pedido) {
  document.getElementById("pedido-id").value = pedido.id || "";
  document.getElementById("nombre").value = `${pedido.nombre} ${pedido.apellido}` || "";
  document.getElementById("direccion").value = pedido.direccion || "";
  document.getElementById("ciudad").value = pedido.ciudad || "";
  document.getElementById("codigo-postal").value = pedido.cp || "";
  document.getElementById("telefono").value = pedido.telefono || "";
  document.getElementById("email").value = pedido.gmail || "";
}

function cargarProductoEnFormulario(producto) {
  document.getElementById("nombre-producto").value = producto.nombre || "";
  document.getElementById("cantidad").value = producto.cantidad || "";
  document.getElementById("nombreColor").value = producto.colorNombre || "";

  // Archivo comprimido (solo preview)
  if (producto.archivoBase64) {
    mostrarArchivoComprimido(producto.archivoBase64, `${producto.nombre || 'archivo'}.zip`);
  } else {
    document.getElementById('comprimido-preview').innerHTML = "";
  }
}


  });
