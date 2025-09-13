// Estado global para colores y archivos
window.productoState = window.productoState || {
  coloresSeleccionados: [],
  archivosSeleccionados: []
};
let preview;
  function actualizarPreview() {
  preview.innerHTML = "";
  if (!window.productoState.archivosSeleccionados || window.productoState.archivosSeleccionados.length === 0) {
    return; // No hay archivos, no mostramos nada
  }
  window.productoState.archivosSeleccionados.forEach((archivo) => {
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

    div.appendChild(img);
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

  const btnEliminar = document.createElement('button');
  btnEliminar.textContent = 'X';
  btnEliminar.title = 'Eliminar archivo comprimido';
  btnEliminar.style.background = 'rgba(255,0,0,0.7)';
  btnEliminar.style.color = 'white';
  btnEliminar.style.border = 'none';
  btnEliminar.style.cursor = 'pointer';
  btnEliminar.style.borderRadius = '4px';
  btnEliminar.style.padding = '0 6px';

  btnEliminar.addEventListener('click', () => {
    window.productoState.archivo = null;
    document.getElementById('archivo-comprimido').value = "";
    preview.innerHTML = "";
    URL.revokeObjectURL(url);
  });

  preview.appendChild(link);
  preview.appendChild(btnEliminar);
}

document.addEventListener("DOMContentLoaded", () => {
  const tablaBody = document.getElementById("tabla-productos");
  const listaColores = document.getElementById("lista-colores");
  preview = document.getElementById("preview-imagenes");

  // Cargar productos y llenar tabla
  async function cargarProductos() {
  try {
    const response = await fetchConRefresh("https://forma-programada.onrender.com/api/productosAprobacion/VerProductos");
    if (!response.ok) throw new Error("Error al obtener los productos");

    const productos = await response.json();
    tablaBody.innerHTML = "";

    productos.forEach(wrapper => {
      const producto = wrapper.producto; // ProductoAprobacioDTO

      const fila = document.createElement("tr");

      fila.innerHTML = `
        <td>${producto.id}</td>
        <td>${producto.nombre}</td>
        <td>${producto.descripcion}</td>
        <td>$${producto.precio.toFixed(2)}</td>
        <td>
            <button class="select">Seleccionar</button>
            <button class="eliminar">Eliminar</button>
        </td>
      `;

      fila.querySelector(".select").addEventListener("click", () => selectProducto(producto.id));
      fila.querySelector(".eliminar").addEventListener("click", () => eliminarProducto(producto.id));

      tablaBody.appendChild(fila);
    });

  } catch (error) {
    console.error("Error al cargar productos:", error.message);
    alert("No se pudieron cargar los productos");
  }
}

window.cargarProductos = cargarProductos;


  cargarProductos();

  async function selectProducto(productoId) {
    try {
        const token = localStorage.getItem("accessToken");
        const res = await fetch(`https://forma-programada.onrender.com/api/productosAprobacion/VerProductoCompleto/${productoId}`, {
            headers: { "Authorization": `Bearer ${token}` }
        });

        if (!res.ok) throw new Error("No se pudo cargar el producto");

        const data = await res.json(); // ya es un objeto, no un array
        if (!data) {
            alert("Producto no encontrado");
            return;
        }

        console.log('ProductoCompletoAprobacionDTO recibido:', data);

        // Colores
        window.productoState.coloresSeleccionados = Array.isArray(data.colores)
            ? [...data.colores]
            : [];

        // Archivos: convertir los base64 en URLs
        window.productoState.archivosSeleccionados = Array.isArray(data.archivos)
            ? data.archivos.map((a, index) => ({
                id: a.id,
                orden: a.orden ?? index,
                linkArchivo: `data:image/png;base64,${a.archivoImagen}` // asumimos imagen PNG
              }))
            : [];

        // Cargar datos en formulario y previews
        cargarProductoEnFormulario(data.producto, window.productoState.coloresSeleccionados, window.productoState.archivosSeleccionados);
        actualizarListaColores();
        actualizarPreview();

        // Mostrar botón editar y guardar productoId
        localStorage.setItem("productoId", productoId);

        // Seleccionar categoría
        await cargarCategoriasYSeleccionar(data.producto.categoriaId);

    } catch (error) {
        console.error(error);
        alert("Error al cargar producto");
    }
}



    // Renderizar lista de colores
function actualizarListaColores() {
  listaColores.innerHTML = "";
  window.productoState.coloresSeleccionados.forEach((colorObj, index) => {
    const li = document.createElement("li");
    li.style.backgroundColor = colorObj.hex;
    li.style.color = "#fff";
    li.style.padding = "5px 10px";
    li.style.borderRadius = "4px";
    li.style.display = "flex";
    li.style.alignItems = "center";
    li.style.justifyContent = "space-between";
    li.style.marginBottom = "6px";
    li.title = colorObj.nombre;

    const span = document.createElement("span");
    span.textContent = colorObj.hex;
    li.appendChild(span);
    listaColores.appendChild(li);
  });
}
window.actualizarListaColores = actualizarListaColores;

// Inicializar la lista si ya hay colores seleccionados
actualizarListaColores();

   


  async function cargarCategoriasYSeleccionar(categoriaIdSeleccionada) {
    try {
      const token = localStorage.getItem("accessToken");
      const res = await fetch("https://forma-programada.onrender.com/api/categoria/combo", {
        headers: { "Authorization": `Bearer ${token}` }
      });
      if (!res.ok) throw new Error("No se pudieron cargar las categorías");
      const categorias = await res.json();

      const select = document.getElementById("categoria");
      select.innerHTML = '<option value="">Seleccionar categoría</option>';

      categorias.forEach(cat => {
        const option = document.createElement("option");
        option.value = cat.id;
        option.textContent = cat.nombre;
        if (cat.id === categoriaIdSeleccionada) {
          option.selected = true; // marcar la categoría del producto
        }
        select.appendChild(option);
      });
    } catch (err) {
      alert("Error cargando categorías: " + err.message);
    }
  }


 // Carga producto en formulario
function cargarProductoEnFormulario(producto, colores, archivos) {
  
  document.getElementById("producto-id").value = producto.id || "";
  document.getElementById("nombre").value = producto.nombre || "";
  document.getElementById("descripcion").value = producto.descripcion || "";
  document.getElementById("precio").value = producto.precio || "";
  document.getElementById("precioDigital").value = producto.precioDigital || "";

  // Nuevos campos
  document.getElementById("codigo-inicial").value = producto.codigoInicial || "";
  document.getElementById("version").value = producto.version || "";
  document.getElementById("seguimiento").value = producto.seguimiento || "";

  document.getElementById("dimension-alto").value = producto.dimensionAlto || "";
  document.getElementById("dimension-ancho").value = producto.dimensionAncho || "";
  document.getElementById("dimension-profundidad").value = producto.dimensionProfundidad || "";

  document.getElementById("material").value = producto.material || "";
  document.getElementById("tecnica").value = producto.tecnica || "";
    // Limpia la unidad si viene como string tipo "1kg", "2.5 kg", etc.
    const pesoLimpio = parseFloat(producto.peso?.toString().replace(/[^\d.]/g, "")) || "";
    document.getElementById("peso").value = pesoLimpio;

    //Archivo ZIP
    if (producto.archivo) {
    mostrarArchivoComprimido(producto.archivo); // función que vos definiste
    } else {
    document.getElementById('comprimido-preview').innerHTML = "";
    }


}

async function eliminarProducto(id) {
  if (!confirm("¿Seguro que querés eliminar este producto?")) return;

  try {
    const token = localStorage.getItem("accessToken");

    const url = new URL("https://forma-programada.onrender.com/api/productosAprobacion/BorrarProducto");
    url.searchParams.append("id", id);

    const res = await fetch(url, {
      method: "DELETE",
      headers: {
        "Authorization": `Bearer ${token}`
      }
    });

    if (!res.ok) throw new Error("Error al eliminar producto");

    alert("Producto eliminado correctamente");
    cargarProductos(); // refrescar la tabla

  } catch (error) {
    alert("Error: " + error.message);
  }
}


});
