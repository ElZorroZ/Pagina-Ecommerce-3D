
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
  const inputImagenes = document.getElementById('imagenes');
  const preview = document.getElementById('preview-imagenes'); // ✅ agregado

  // Estado global
  window.productoState = window.productoState || {};
  window.productoState.coloresSeleccionados = window.productoState.coloresSeleccionados || [];
  window.productoState.archivosSeleccionados = window.productoState.archivosSeleccionados || [];

  inputImagenes.addEventListener('change', () => {
    const files = Array.from(inputImagenes.files);

    files.forEach(file => {
      if (window.productoState.archivosSeleccionados.length < 5) {
        const existe = window.productoState.archivosSeleccionados.some(f => f.name === file.name && f.size === file.size);
        if (!existe) window.productoState.archivosSeleccionados.push(file);
      }
    });

    actualizarPreview();
    inputImagenes.value = "";
  });

  async function cargarCategorias() {
    try {
      const token = localStorage.getItem("accessToken"); // o donde tengas el token guardado
      const res = await fetch(`${API_BASE_URL}/api/categoria/combo`, {
        headers: {
          "Authorization": `Bearer ${token}`
        }
      });
      if (!res.ok) throw new Error("No se pudieron cargar las categorías");
      const categorias = await res.json();

      const select = document.getElementById("categoria");
      select.innerHTML = '<option value="">Seleccionar categoría</option>';

      categorias.forEach(cat => {
        const option = document.createElement("option");
        option.value = cat.id;
        option.textContent = cat.nombre;
        select.appendChild(option);
      });
    } catch (err) {
      alert("Error cargando categorías: " + err.message);
    }
  }


  async function subirArchivoBackend(productoId, file, orden) {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("orden", orden);

    const res = await fetchConRefresh(`${API_BASE_URL}/api/productosAprobacion/${productoId}/archivos`, {
      method: "POST",
      body: formData,
    });

    if (!res.ok) {
      let errorText = await res.text();
      console.error("Error backend text:", errorText);
      try {
        const errorData = JSON.parse(errorText);
        throw new Error(errorData.message || "Error subiendo archivo al backend");
      } catch {
        throw new Error(errorText || "Error subiendo archivo al backend");
      }
    }
  }

  const form = document.getElementById("form-producto");
  const inputColorText = document.getElementById("input-color-text");
  const inputColorNombre = document.getElementById("input-color-nombre");
  const btnAgregarColor = document.getElementById("btn-agregar-color");
  const listaColores = document.getElementById("lista-colores");

// Función para convertir cualquier color a HEX
function colorToHex(colorStr) {
  const ctx = document.createElement("canvas").getContext("2d");
  ctx.fillStyle = colorStr;
  return ctx.fillStyle;
}

// Agregar color usando Pickr
btnAgregarColor.addEventListener("click", () => {
  let colorInput = inputColorText.value.trim();
  if (!colorInput) return;

  let hex;
  try {
    hex = colorToHex(colorInput);
  } catch {
    alert("Color inválido. Puede ser HEX, RGB o HSL");
    return;
  }

  const nombre = inputColorNombre.value.trim() || colorInput;
  window.productoState.coloresSeleccionados.push({ hex: hex.toUpperCase(), nombre });
  actualizarListaColores();

  // 🔹 Actualiza Pickr global con este color (solo visual)
  if (window.pickrInstance) window.pickrInstance.setColor(hex.toUpperCase());

  inputColorNombre.value = "";
  inputColorText.value = "";
});


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

    const btnBorrar = document.createElement("button");
    btnBorrar.textContent = "x";
    btnBorrar.style.backgroundColor = "#dc3545";
    btnBorrar.style.color = "#fff";
    btnBorrar.style.border = "none";
    btnBorrar.style.borderRadius = "50%";
    btnBorrar.style.width = "20px";
    btnBorrar.style.height = "20px";
    btnBorrar.style.cursor = "pointer";
    btnBorrar.style.display = "flex";
    btnBorrar.style.alignItems = "center";
    btnBorrar.style.justifyContent = "center";
    btnBorrar.style.flexShrink = "0";
    btnBorrar.addEventListener("click", () => {
      window.productoState.coloresSeleccionados.splice(index, 1);
      actualizarListaColores();
    });

    li.appendChild(span);
    li.appendChild(btnBorrar);
    listaColores.appendChild(li);
  });
}
window.actualizarListaColores = actualizarListaColores;

// Inicializar la lista si ya hay colores seleccionados
actualizarListaColores();

  function actualizarPreview() {
    preview.innerHTML = "";
    if (!window.productoState.archivosSeleccionados || window.productoState.archivosSeleccionados.length === 0) return;

    window.productoState.archivosSeleccionados.forEach((archivo, idx) => {
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
        window.productoState.archivosSeleccionados.splice(idx, 1);
        actualizarPreview();
      });

      div.appendChild(img);
      div.appendChild(btnEliminar);
      preview.appendChild(div);
    });
  }
  cargarCategorias();
  const inputArchivoComprimido = document.getElementById("archivo-comprimido");
  const previewComprimido = document.getElementById("comprimido-preview");

  inputArchivoComprimido.addEventListener("change", function () {
    const archivo = this.files[0];
    const extensionesPermitidas = [".zip", ".rar", ".7z", ".tar", ".gz", ".bz2"];
    if (archivo && extensionesPermitidas.some(ext => archivo.name.endsWith(ext))) {
      window.productoState.archivoComprimido = archivo;
      actualizarPreviewComprimido();
    } else {
      alert("El archivo debe ser uno de estos: .zip,.rar,.7z,.tar,.gz,.bz2");
      this.value = ""; // limpiar input
    }
  });

  function actualizarPreviewComprimido() {
    previewComprimido.innerHTML = "";
    const archivo = window.productoState.archivoComprimido;
    if (!archivo) return;

    const div = document.createElement("div");
    div.style.position = "relative";
    div.style.display = "inline-block";
    div.style.marginRight = "10px";
    div.style.padding = "6px 10px";
    div.style.border = "1px solid #ccc";
    div.style.borderRadius = "4px";
    div.style.background = "#f9f9f9";
    div.style.fontFamily = "monospace";

    const nombreArchivo = document.createElement("span");
    nombreArchivo.textContent = archivo.name;

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
    btnEliminar.title = "Eliminar archivo ZIP";
    btnEliminar.addEventListener("click", () => {
      window.productoState.archivoComprimido = null;
      inputArchivoComprimido.value = "";
      actualizarPreviewComprimido();
    });

    div.appendChild(nombreArchivo);
    div.appendChild(btnEliminar);
    previewComprimido.appendChild(div);
  }

  form.addEventListener("submit", async (e) => {
  e.preventDefault();

  const nombre = document.getElementById("nombre").value.trim();
  const descripcion = document.getElementById("descripcion").value.trim();
  const precio = parseFloat(document.getElementById("precio").value);
  const precioDigital = parseFloat(document.getElementById("precioDigital").value);

  const codigoInicial = document.getElementById("codigo-inicial").value.trim();
  const versionInput = document.getElementById('version');
  const versionValue = versionInput.value.trim();

  if (!/^\d{1,4}$/.test(versionValue)) {
    alert("La versión debe ser un número de hasta 4 dígitos.");
    return;
  }

  const version = versionValue;
  const seguimiento = document.getElementById("seguimiento").value.trim();

  const dimensionAlto = parseInt(document.getElementById("dimension-alto").value);
  const dimensionAncho = parseInt(document.getElementById("dimension-ancho").value);
  const dimensionProfundidad = parseInt(document.getElementById("dimension-profundidad").value);

  const material = document.getElementById("material").value.trim();
  const peso = parseFloat(document.getElementById("peso").value);
  const tecnica = document.getElementById("tecnica").value.trim();

  if (!nombre || isNaN(precio)) {
    alert("Por favor completa todos los campos obligatorios.");
    return;
  }
const tieneArchivosDeOtroProducto = window.productoState.archivosSeleccionados
  ?.filter(a => !a.eliminado) // aunque en crear no debería haber eliminados
  .some(a => !(a instanceof File)) || false;

if (tieneArchivosDeOtroProducto) {
  alert("Estás usando imágenes que pertenecen a otro producto. Por favor eliminá esas imágenes antes de guardar uno nuevo.");
  return;
}


  const categoriaId = parseInt(document.getElementById("categoria").value);
  if (!categoriaId) {
    alert("Seleccioná una categoría");
    return;
  }
const usuarioLogueadoId = parseInt(localStorage.getItem("usuarioId"), 10);

  try {
    // Armar FormData para enviar JSON + archivo
    const formData = new FormData();

    // Crear el objeto JSON con los datos
    const productoPayload = {
      nombre,
      descripcion,
      precio,
      precioDigital,
      categoriaId,
      colores: window.productoState.coloresSeleccionados, // ✅ colores
      codigoInicial,
      version,
      seguimiento,
      dimensionAlto,
      dimensionAncho,
      dimensionProfundidad,
      material,
      peso,
      tecnica,
      creadorId: usuarioLogueadoId
    };
    console.log("ProductoPayload a enviar:", productoPayload);

    // El JSON va como string en una parte llamada "producto" (podés cambiar el nombre si querés)
    formData.append("producto", new Blob([JSON.stringify(productoPayload)], { type: "application/json" }));

    // Agregar archivo ZIP (input con id "archivo-comprimido")
    const archivoComprimidoInput = document.getElementById("archivo-comprimido");
    if (archivoComprimidoInput && archivoComprimidoInput.files.length > 0) {
      formData.append("archivo", archivoComprimidoInput.files[0]);
    }

    const backendBase = `${API_BASE_URL}/api/productosAprobacion/crearAprobacionProducto`;
    
    const resProducto = await fetchConRefresh(backendBase, {
      method: "POST",
      // NO seteamos Content-Type, fetch lo hará automáticamente con boundary correcto
      body: formData,
    });

    if (!resProducto.ok) {
      let errorText = await resProducto.text();
      try {
        const json = JSON.parse(errorText);
        errorText = json.message || JSON.stringify(json);
      } catch {}
      throw new Error(errorText || "Error al guardar el producto");
    }

    const productoCreado = await resProducto.json();

    // Filtrar solo los que sean instancias de File
    const archivosValidos = window.productoState.archivosSeleccionados.filter(f => f instanceof File);

    if (archivosValidos.length > 0) {
      for (let i = 0; i < Math.min(archivosValidos.length, 5); i++) {
        await subirArchivoBackend(productoCreado.id, archivosValidos[i], i);
      }
    }


    alert("Producto, colores, archivo Comprimido y imagenes guardados con éxito!");
    form.reset();
    window.productoState.coloresSeleccionados = [];
    window.productoState.archivosSeleccionados = [];
    actualizarListaColores();
    actualizarPreview();
    cargarProductos();
  } catch (error) {
    alert("Error: " + error.message);
  }
});

})();

});
