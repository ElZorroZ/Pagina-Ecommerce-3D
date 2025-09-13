<<<<<<< HEAD
document.addEventListener("DOMContentLoaded", () => {
(() => {
  // --- Referencias DOM ---
=======

document.addEventListener("DOMContentLoaded", () => {

// Función para refrescar el access token usando el refresh token
async function refreshAccessToken() {
  const refreshToken = localStorage.getItem("refreshToken");
  if (!refreshToken) {
    // No redirige automáticamente, podés agregarlo si querés
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
      const res = await fetch("http://localhost:8080/api/categoria/combo", {
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

    const res = await fetchConRefresh(`http://localhost:8080/api/productosAprobacion/${productoId}/archivos`, {
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

>>>>>>> parent of 391f6a9 (Merge branch 'main' of https://github.com/ElZorroZ/Pagina-Ecommerce-3D)
  const form = document.getElementById("form-producto");
  const inputImagenes = document.getElementById('imagenes');
  const preview = document.getElementById('preview-imagenes');
  const inputArchivoComprimido = document.getElementById("archivo-comprimido");
  const previewComprimido = document.getElementById("comprimido-preview");
  const inputColorText = document.getElementById("input-color-text");
  const inputColorNombre = document.getElementById("input-color-nombre");
  const btnAgregarColor = document.getElementById("btn-agregar-color");
  const listaColores = document.getElementById("lista-colores");

  // --- Estado global ---
  window.productoState = window.productoState || {};
  window.productoState.coloresSeleccionados = window.productoState.coloresSeleccionados || [];
  window.productoState.archivosSeleccionados = window.productoState.archivosSeleccionados || [];
  window.productoState.archivoComprimido = window.productoState.archivoComprimido || null;

  // --- Color Manager ---
  // --- Color Manager ---
    window.colorManager = window.colorManager || {};
    window.colorManager.colorToHex = (colorStr) => {
      const ctx = document.createElement("canvas").getContext("2d");
      ctx.fillStyle = colorStr;
      return ctx.fillStyle.toUpperCase();
    };
    window.colorManager.agregarColor = (colorStr, nombreStr) => {
      if (!colorStr) return;
      let hex;
      try { hex = window.colorManager.colorToHex(colorStr); } 
      catch { alert("Color inválido. Puede ser HEX, RGB o HSL"); return; }
      const nombre = nombreStr?.trim() || colorStr;
      window.productoState.coloresSeleccionados.push({ hex, nombre });
      window.colorManager.actualizarListaColores();
      if (window.pickrInstance) window.pickrInstance.setColor(hex);
    };
    window.colorManager.actualizarListaColores = () => {
      const listaColores = document.getElementById("lista-colores");
      if (!listaColores) return;
      listaColores.innerHTML = "";
      window.productoState.coloresSeleccionados.forEach((c, idx) => {
        const li = document.createElement("li");
        li.style.cssText = "display:flex; align-items:center; justify-content:space-between; margin-bottom:6px; padding:5px 10px; border-radius:4px; background:#f0f0f0";
        const colorBox = document.createElement("div");
        colorBox.style.cssText = `width:20px;height:20px;border-radius:4px;margin-right:10px;background-color:${c.hex}`;
        const span = document.createElement("span");
        span.textContent = c.nombre;
        span.style.flexGrow = "1";
        const btnBorrar = document.createElement("button");
        btnBorrar.textContent = "x";
        btnBorrar.style.cssText = "background:#dc3545;color:#fff;border:none;border-radius:50%;width:20px;height:20px;cursor:pointer";
        btnBorrar.addEventListener("click", () => {
          window.productoState.coloresSeleccionados.splice(idx, 1);
          window.colorManager.actualizarListaColores();
        });
        li.append(colorBox, span, btnBorrar);
        listaColores.appendChild(li);
      });
    };

  // --- Agregar color ---
  if (btnAgregarColor) {
    btnAgregarColor.addEventListener("click", () => {
      window.colorManager.agregarColor(inputColorText.value, inputColorNombre.value);
      inputColorText.value = "";
      inputColorNombre.value = "";
    });
  }

  window.colorManager.actualizarListaColores();

  // --- Previews de imágenes ---
  if (inputImagenes) {
    inputImagenes.addEventListener("change", () => {
      const files = Array.from(inputImagenes.files);
      files.forEach(file => {
        const existe = window.productoState.archivosSeleccionados.some(f => f.name === file.name && f.size === file.size);
        if (existe) mostrarError(`La imagen "${file.name}" ya fue seleccionada.`);
        else if (window.productoState.archivosSeleccionados.length >= 5) mostrarError("No se pueden seleccionar más de 5 imágenes.");
        else window.productoState.archivosSeleccionados.push(file);
      });
      actualizarPreview();
      inputImagenes.value = "";
    });
  }

  // --- Preview archivo comprimido ---
  if (inputArchivoComprimido) {
    inputArchivoComprimido.addEventListener("change", function() {
      const archivo = this.files[0];
      const extPermitidas = [".zip", ".rar", ".7z", ".tar", ".gz", ".bz2"];
      if (archivo && extPermitidas.some(ext => archivo.name.toLowerCase().endsWith(ext))) {
        window.productoState.archivoComprimido = archivo;
        window.actualizarPreviewComprimido();
      } else {
        mostrarError("Archivo inválido. Debe ser: " + extPermitidas.join(", "));
        this.value = "";
      }
    });
  }

  window.actualizarPreviewComprimido = () => {
    previewComprimido.innerHTML = "";
    const archivo = window.productoState.archivoComprimido;
    if (!archivo) return;
    const div = document.createElement("div");
    div.style.cssText = "position:relative;display:inline-block;margin-right:10px;padding:6px 10px;border:1px solid #ccc;border-radius:4px;background:#f9f9f9;font-family:monospace";
    const span = document.createElement("span");
    span.textContent = archivo.name;
    const btnEliminar = document.createElement("button");
    btnEliminar.textContent = "X";
    btnEliminar.style.cssText = "position:absolute;top:0;right:0;background:rgba(255,0,0,0.7);color:white;border:none;cursor:pointer;border-radius:0 4px 0 4px;padding:0 4px";
    btnEliminar.addEventListener("click", () => {
      window.productoState.archivoComprimido = null;
      inputArchivoComprimido.value = "";
      window.actualizarPreviewComprimido();
    });
    div.append(span, btnEliminar);
    previewComprimido.appendChild(div);
  };

  // --- Función para subir imágenes al backend ---
  // Exponerla globalmente
window.subirArchivoBackend = async function(productoId, file, orden) {
  try {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("orden", orden);

    const res = await authManager.fetchWithAuth(`${API_BASE_URL}/api/productosAprobacion/${productoId}/archivos`, { method: "POST", body: formData });
    if (!res.ok) {
      let errorMessage = "Error subiendo archivo al backend";
      try { const data = await res.clone().json(); if (data.message) errorMessage = data.message; } catch { try { const text = await res.text(); if (text) errorMessage = text; } catch {} }
      mostrarError("❌ " + errorMessage);
      throw new Error(errorMessage);
    }
    mostrarExito(`✅ Archivo "${file.name}" subido correctamente`);
  } catch (error) {
    mostrarError("❌ Error en subirArchivoBackend: " + error.message);
    throw error;
  }
};

// --- Submit del form ---
if (form) {
  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    try {
      // --- Recolectar campos ---
      const nombre = document.getElementById("nombre").value.trim();
      const descripcion = document.getElementById("descripcion").value.trim();
      const categoriaId = parseInt(document.getElementById("categoria").value);
      const precio = parseFloat(document.getElementById("precio").value);
      const precioDigital = parseFloat(document.getElementById("precioDigital").value);
      const codigoInicial = document.getElementById("codigo-inicial").value.trim();
      const version = document.getElementById("version").value.trim();
      const seguimiento = document.getElementById("seguimiento").value.trim();
      const dimensionAlto = parseFloat(document.getElementById("dimension-alto").value);
      const dimensionAncho = parseFloat(document.getElementById("dimension-ancho").value);
      const dimensionProfundidad = parseFloat(document.getElementById("dimension-profundidad").value);
      const material = document.getElementById("material").value.trim();
      const peso = parseFloat(document.getElementById("peso").value);
      const tecnica = document.getElementById("tecnica").value.trim();

      // --- Validaciones básicas ---
      if (!nombre || isNaN(precio) || !categoriaId) throw new Error("Completa todos los campos obligatorios (nombre, precio y categoría).");
      if (isNaN(dimensionAlto) || isNaN(dimensionAncho) || isNaN(dimensionProfundidad)) throw new Error("Ingresa dimensiones válidas.");
      if (isNaN(peso)) throw new Error("Ingresa un peso válido.");

      const colores = window.productoState.coloresSeleccionados || [];

      // --- Armar payload del producto ---
      const productoPayload = {
        creadorId: parseInt(authManager.getUserId()), // <--- agregar esto
        nombre,
        descripcion,
        precio,
        precioDigital,
        categoriaId,
        codigoInicial,
        version,
        seguimiento,
        dimensionAlto,
        dimensionAncho,
        dimensionProfundidad,
        material,
        peso,
        tecnica,
        colores
      };

      // --- FormData para envío ---
      const formData = new FormData();
      formData.append("producto", new Blob([JSON.stringify(productoPayload)], { type: "application/json" }));
      if (window.productoState.archivoComprimido) formData.append("archivo", window.productoState.archivoComprimido);

      // --- Crear producto usando fetchWithAuth global ---
      const resProducto = await fetchWithAuth(`${API_BASE_URL}/api/productosAprobacion/crearAprobacionProducto`, {
        method: "POST",
        body: formData
      });

      if (!resProducto.ok) {
        let errorText = await resProducto.text();
        throw new Error(errorText || "Error desconocido al crear el producto.");
      }

      const productoCreado = await resProducto.json();
console.log("Producto creado:", productoCreado); // Debe mostrar id

      // --- Subir imágenes ---
      for (let i = 0; i < (window.productoState.archivosSeleccionados || []).length; i++) {
        await window.subirArchivoBackend(productoCreado.id, window.productoState.archivosSeleccionados[i], i);
      }

      // --- Resetear estado ---
      mostrarExito("Producto, colores, archivo comprimido e imágenes guardados con éxito!");
      form.reset();
      window.productoState.coloresSeleccionados = [];
      window.productoState.archivosSeleccionados = [];
      window.productoState.archivoComprimido = null;
      window.colorManager.actualizarListaColores();
      window.actualizarPreview();
      window.actualizarPreviewComprimido();

      // --- Recargar lista de productos de manera global ---
      window.cargarProductos && window.cargarProductos();

    } catch (err) {
      mostrarError("Error al guardar el producto: " + (err.message || "Error desconocido"));
      console.error("Submit error:", err);
    }
  });
}

<<<<<<< HEAD
=======

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

    const backendBase = "http://localhost:8080/api/productosAprobacion/crearAprobacionProducto";
    
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

>>>>>>> parent of 391f6a9 (Merge branch 'main' of https://github.com/ElZorroZ/Pagina-Ecommerce-3D)
})();
});

