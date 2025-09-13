// Estado global para colores y archivos
window.productoState = window.productoState || {
  coloresSeleccionados: [],
  archivosSeleccionados: []
};
let preview;
const API_BASE_URL = "https://forma-programada.onrender.com";

  // Preview archivos
function actualizarPreview() {
  preview.innerHTML = "";

  if (
    !window.productoState.archivosSeleccionados ||
    window.productoState.archivosSeleccionados.length === 0
  ) {
    return; // No hay archivos, no mostramos nada
  }

  window.productoState.archivosSeleccionados.forEach((archivo) => {
    // 🚨 Si está marcado como eliminado, no se renderiza
    if (archivo.eliminado) return;

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
      // Imagen recién subida por el usuario
      const reader = new FileReader();
      reader.onload = (e) => {
        img.src = e.target.result;
      };
      reader.readAsDataURL(archivo);
    } else if (archivo.archivoImagen) {
      // Imagen en base64 desde backend
      img.src = `data:image/jpeg;base64,${archivo.archivoImagen}`;
    } else {
      // Imagen con link ya existente
      img.src = archivo.linkArchivo || archivo.url || "ruta_default.jpg";
    }

    div.appendChild(img);

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
        if (archivo.id) {
          // Si viene del backend, solo se marca como eliminado
          archivo.eliminado = true;
        } else {
          // Si es un File nuevo, se borra directamente del array
          window.productoState.archivosSeleccionados =
            window.productoState.archivosSeleccionados.filter(
              (a) => a !== archivo
            );
        }
        actualizarPreview(); // refrescamos preview
      }
    });

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


// Función para mostrar el ZIP en el preview
function mostrarArchivoComprimido(base64, nombre = 'archivo.zip') {
  if (!base64) return;

  // Decodificar Base64 a binario
  const byteCharacters = atob(base64);
  const byteNumbers = new Array(byteCharacters.length);
  for (let i = 0; i < byteCharacters.length; i++) {
    byteNumbers[i] = byteCharacters.charCodeAt(i);
  }
  const byteArray = new Uint8Array(byteNumbers);

  // Crear Blob y URL de descarga
  const blob = new Blob([byteArray], { type: 'application/zip' });
  const url = URL.createObjectURL(blob);

  // Limpiar preview previo
  const preview = document.getElementById('comprimido-preview');
  preview.innerHTML = '';

  // Crear link de descarga
  const link = document.createElement('a');
  link.href = url;
  link.download = nombre;
  link.textContent = `Descargar ${nombre}`;
  link.style.display = 'inline-block';
  link.style.marginRight = '10px';

  // Botón para eliminar
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
    window.productoState.archivoComprimido = null;
    const inputFile = document.getElementById('archivo-comprimido');
    if (inputFile) inputFile.value = "";
    preview.innerHTML = "";
    URL.revokeObjectURL(url);
  });

  // Agregar al DOM
  preview.appendChild(link);
  preview.appendChild(btnEliminar);
}

document.addEventListener("DOMContentLoaded", () => {
  const tablaBody = document.getElementById("tabla-productos");
  const listaColores = document.getElementById("lista-colores");
  const inputColor = document.getElementById("input-color");
  const btnEditar = document.getElementById("btn-editar-producto");
  preview = document.getElementById("preview-imagenes");
   const inputArchivos = document.getElementById("imagenes");
    console.log("inputColor:", inputColor);

  inputArchivos.addEventListener("change", (e) => {
    const archivosNuevos = Array.from(e.target.files);
    archivosNuevos.forEach(file => window.productoState.archivosSeleccionados.push(file));
    actualizarPreview();
    e.target.value = ""; // para poder subir más archivos luego
  });

// Cargar productos y llenar tabla
async function cargarProductos() {
  try {
    // Tomo el id del usuario desde localStorage
  const usuarioId = parseInt(localStorage.getItem("usuarioId"), 10);

    const response = await fetchConRefresh(`${API_BASE_URL}/api/productosAprobacion/VerProductos_de/${usuarioId}`);
    if (!response.ok) throw new Error("Error al obtener los productos");

    const productos = await response.json();
    tablaBody.innerHTML = "";

    productos.forEach(item => {
      // El DTO tiene la info del producto dentro de "producto"
      const p = item.producto;

      const fila = document.createElement("tr");
      fila.innerHTML = `
        <td>${p.id}</td>
        <td>${p.nombre}</td>
        <td>${p.descripcion}</td>
        <td>$${Math.floor(p.precio)}</td>
        <td>
            <button class="select">Seleccionar</button>
            <button class="eliminar">Eliminar</button>
        </td>
      `;

      fila.querySelector(".select").addEventListener("click", () => selectProducto(p.id));
      fila.querySelector(".eliminar").addEventListener("click", () => eliminarProducto(p.id));

      tablaBody.appendChild(fila);
    });

  } catch (error) {
    console.error("Error al cargar productos frontend:", error.message);
    alert("No se pudieron cargar los productos");
  }
  window.cargarProductos = cargarProductos;
}

cargarProductos();

  // Seleccionar producto y cargar en formulario + preview
  async function selectProducto(productoId) {
    try {
      const token = localStorage.getItem("accessToken");
      const res = await fetch(`${API_BASE_URL}/api/productosAprobacion/VerProductoCompleto/${productoId}`, {
        headers: { "Authorization": `Bearer ${token}` }
      });
      if (!res.ok) throw new Error("No se pudo cargar el producto");
      const data = await res.json();
      console.log('ProductoCompletoDTO recibido:', data);
      window.productoState.coloresSeleccionados = Array.isArray(data.colores)
      ? [...data.colores]  // si es array (vacío o con elementos), actualiza
      : [];                // si no es array, limpiar (opcional)
      window.productoState.archivosSeleccionados = Array.isArray(data.archivos) && data.archivos.length > 0
      ? data.archivos.map(a => ({
          id: a.id,
          archivoImagen: a.archivoImagen, // base64 del backend
          imagenPreview: a.archivoImagen ? `data:image/jpeg;base64,${a.archivoImagen}` : null, // listo para <img src="">
          orden: a.orden
        }))
      : [];


      cargarProductoEnFormulario(data.producto, window.productoState.coloresSeleccionados, window.productoState.archivosSeleccionados);
      window.actualizarListaColores();
      actualizarPreview();
      cargarProductoPreview(data.producto, window.productoState.coloresSeleccionados, window.productoState.archivosSeleccionados)
      btnEditar.style.display = "block";
      localStorage.setItem("productoId", productoId);
      await cargarCategoriasYSeleccionar(data.producto.categoriaId);
    } catch (error) {
      console.error(error);
      alert("Error al cargar producto");
    }
  }

  async function cargarCategoriasYSeleccionar(categoriaIdSeleccionada) {
    try {
      const token = localStorage.getItem("accessToken");
      const res = await fetch(`${API_BASE_URL}/api/categoria/combo`, {
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
  document.getElementById("peso").value = producto.peso || "";

  // ZIP preview (si querés mostrarlo como enlace para descargar)
  if (producto.archivo) {
    console.log("Archivo ZIP recibido:", producto.archivo);
    window.productoState.archivoComprimido = producto.archivo;
    mostrarArchivoComprimido(producto.archivo);
  } else {
      document.getElementById('comprimido-preview').innerHTML = "";
        window.productoState.archivoComprimido = null;

  }


}

async function actualizarProducto() {
  const id = document.getElementById("producto-id").value;
  const nombre = document.getElementById("nombre").value.trim();
  const descripcion = document.getElementById("descripcion").value.trim();
  const precio = parseFloat(document.getElementById("precio").value);
  const precioDigital = parseFloat(document.getElementById("precioDigital").value);
  const categoriaId = parseInt(document.getElementById("categoria").value);

  if (!nombre || isNaN(precio)) {
    alert("Completa los campos obligatorios correctamente.");
    return;
  }

  // Archivos existentes que ya tienen base64 y id
const archivosExistentes = window.productoState.archivosSeleccionados
  .filter(a => !(a instanceof File))
  .map(a => ({
    id: a.id,
    archivoImagen: a.archivoImagen,
    orden: a.orden,
    eliminado: !!a.eliminado  // agregamos esta propiedad
  }));



  // Archivos nuevos tipo File
  const archivosNuevos = window.productoState.archivosSeleccionados
    .filter(a => a instanceof File);

  // Armar DTO con datos del producto
  const productoCompletoDTO = {
    producto: {
      nombre,
      descripcion,
      precio,
      precioDigital,
      categoriaId,
      codigoInicial: document.getElementById("codigo-inicial").value.trim(),
      version: document.getElementById("version").value.trim(),
      seguimiento: document.getElementById("seguimiento").value.trim(),
      dimensionAlto: document.getElementById("dimension-alto").value.trim(),
      dimensionAncho: document.getElementById("dimension-ancho").value.trim(),
      dimensionProfundidad: document.getElementById("dimension-profundidad").value.trim(),
      material: document.getElementById("material").value.trim(),
      peso: document.getElementById("peso").value.trim(),
      tecnica: document.getElementById("tecnica").value.trim(),
      archivoComprimido: null, // No lo mandamos en el JSON, va aparte en formData
    },
    colores: window.productoState.coloresSeleccionados,
    archivos: archivosExistentes
  };

  // Crear FormData y agregar JSON del DTO como Blob
  const formData = new FormData();
  formData.append(
    "producto",
    new Blob([JSON.stringify(productoCompletoDTO)], { type: "application/json" })
  );

  // Agregar archivos nuevos (archivos tipo File)
  archivosNuevos.forEach(file => {
    formData.append("archivosNuevos", file);
  });

  // Agregar archivo comprimido (zip o similar)
  const archivoComprimidoInput = document.getElementById("archivo-comprimido");
  if (archivoComprimidoInput && archivoComprimidoInput.files.length > 0) {
    formData.append("archivoComprimido", archivoComprimidoInput.files[0]);
  }
      if (window.productoState.archivoComprimido === null) {
    formData.append("eliminarArchivoComprimido", "true");
}
  try {
    const res = await fetchConRefresh(`${API_BASE_URL}/api/productosAprobacion/ActualizarProductoAprobar/${id}`, {
      method: "PUT",
      body: formData,
      // NO configures Content-Type, el navegador lo hace automáticamente para multipart/form-data
    });

    if (!res.ok) {
      let errorMessage = "Error actualizando producto";
      try {
        const errorData = await res.json();
        if (errorData.message) errorMessage = errorData.message;
      } catch {}
      throw new Error(errorMessage);
    }

    alert("Producto actualizado correctamente");
    cargarProductos();

  } catch (error) {
    alert("Error: " + error.message);
  }
}

if (btnEditar) {
  btnEditar.addEventListener("click", e => {
    e.preventDefault();
    actualizarProducto();
  });
}



function cargarProductoPreview(producto, colores, archivos) {
  console.log("👉 Ejecutando cargarProductoPreview", { producto, colores, archivos });
    console.log("Producto:", producto);
  console.log("Colores:", colores);
  console.log("Archivos:", archivos);

  document.getElementById("prev-nombre").textContent = producto.nombre || "";
  document.getElementById("prev-desc").textContent = producto.descripcion || "";
  document.getElementById("prev-precio").textContent = `$${(producto.precio || 0).toFixed(2)}`;

  const mainImage = document.getElementById("main-image");
  const miniaturasDiv = document.getElementById("miniaturas");
  miniaturasDiv.innerHTML = "";

  // Buscar imágenes válidas (con linkArchivo o si es File)
  const imagenesValidas = archivos
    .filter(a => a instanceof File || a.linkArchivo || a.url);

  if (imagenesValidas.length > 0) {
    const primeraSrc = imagenesValidas[0] instanceof File
      ? URL.createObjectURL(imagenesValidas[0])
      : imagenesValidas[0].linkArchivo || imagenesValidas[0].url;

    mainImage.src = primeraSrc;

    imagenesValidas.forEach((archivo) => {
      const src = archivo instanceof File
        ? URL.createObjectURL(archivo)
        : archivo.linkArchivo || archivo.url;

      const thumb = document.createElement("img");
      thumb.src = src;
      thumb.className = "thumbnail-image";
      thumb.onclick = () => mainImage.src = thumb.src;
      miniaturasDiv.appendChild(thumb);
    });
  } else {
    mainImage.src = "ruta_default.jpg"; // imagen por defecto
  }

  // Formatos (hardcoded)
  const formatos = ["STL", "OBJ", "AMF"];
  const formatoDiv = document.getElementById("option-formato");
  formatoDiv.innerHTML = "";
  formatos.forEach((formato, i) => {
    const id = `formato-${i}`;
    formatoDiv.innerHTML += `
      <label for="${id}">
        <input type="radio" name="formato" id="${id}" value="${formato}" ${i === 0 ? "checked" : ""} />
        ${formato}
      </label>
    `;
  });

  // Colores
  const colorDiv = document.getElementById("option-color");
  colorDiv.innerHTML = "";
  if (colores && colores.length > 0) {
    colores.forEach((color, i) => {
      const id = `color-${i}`;
      colorDiv.innerHTML += `
        <label for="${id}">
          <input type="radio" name="color" id="${id}" value="${color}" ${i === 0 ? "checked" : ""} />
          ${color}
        </label>
      `;
    });
  } else {
    colorDiv.textContent = "No hay colores disponibles";
  }
}
async function eliminarProducto(id) {
  if (!confirm("¿Seguro que querés eliminar este producto?")) return;
  try {
    const token = localStorage.getItem("accessToken");
    const res = await fetch(`${API_BASE_URL}/api/productosAprobacion/BorrarProducto/Colaborador?id=${id}`, {
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
