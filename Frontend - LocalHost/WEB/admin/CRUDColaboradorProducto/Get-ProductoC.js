// Estado global para colores y archivos
window.productoState = window.productoState || {
  coloresSeleccionados: [],
  archivosSeleccionados: []
};
let preview;

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
  const categoriesDropdown = document.querySelector("#categories-dropdown .dropdown-content");
  const shopTrigger = document.getElementById("shop-trigger");
  preview = document.getElementById("preview-imagenes");
   const inputArchivos = document.getElementById("imagenes");
    console.log("inputColor:", inputColor);

  inputArchivos.addEventListener("change", (e) => {
    const archivosNuevos = Array.from(e.target.files);
    archivosNuevos.forEach(file => window.productoState.archivosSeleccionados.push(file));
    actualizarPreview();
    e.target.value = ""; // para poder subir más archivos luego
  });
// --- Cargar productos y llenar tabla ---
// --- Cargar productos y llenar tabla ---
let selectedProductoId = null; // Guardamos el producto seleccionado


async function cargarProductos() {
  try {
    const usuarioId = parseInt(authManager.getUserId(), 10);
    if (!usuarioId) throw new Error("Usuario no logueado");

    const response = await fetchWithAuth(`${API_BASE_URL}/api/productosAprobacion/VerProductos_de/${usuarioId}`);
    if (!response.ok) throw new Error("Error al obtener los productos");

    const productos = await response.json();
    tablaBody.innerHTML = "";

    productos.forEach(item => {
      const p = item.producto;

      const fila = document.createElement("tr");
      fila.dataset.productoId = p.id; // guardamos el id en el tr
      fila.innerHTML = `
        <td>${p.id}</td>
        <td title="${p.nombre}">${p.nombre}</td>
        <td title="${p.descripcion}">${p.descripcion.length > 50 ? p.descripcion.slice(0, 50) + "..." : p.descripcion}</td>
        <td>$${Math.floor(p.precio)}</td>
        <td>
            <button class="select">Seleccionar</button>
            <button class="eliminar">Eliminar</button>
        </td>
      `;

      const selectBtn = fila.querySelector(".select");
      const eliminarBtn = fila.querySelector(".eliminar");

      // Mostrar/ocultar botón según el producto seleccionado
      selectBtn.style.display = (selectedProductoId === p.id) ? "none" : "inline-block";

      selectBtn.addEventListener("click", async () => {
        const anteriorId = selectedProductoId;
        selectedProductoId = p.id;

        // Ocultamos el botón del producto seleccionado
        selectBtn.style.display = "none";

        // Volvemos a mostrar el botón del producto previamente seleccionado
        if (anteriorId !== null && anteriorId !== p.id) {
          const filaAnterior = tablaBody.querySelector(`tr[data-producto-id="${anteriorId}"]`);
          if (filaAnterior) filaAnterior.querySelector(".select").style.display = "inline-block";
        }

        mostrarExito(`Producto "${p.nombre}" seleccionado.`);
        await selectProducto(p.id); // cargamos producto en el formulario
      });

      eliminarBtn.addEventListener("click", () => eliminarProducto(p.id));

      tablaBody.appendChild(fila);
    });

  } catch (error) {
    console.error("Error al cargar productos frontend:", error);
    mostrarError("No se pudieron cargar los productos: " + (error.message || "Error desconocido"));
  }
}

// Exponer globalmente
window.cargarProductos = cargarProductos;
cargarProductos();

// --- Seleccionar producto y cargar en formulario + preview ---
async function selectProducto(productoId) {
  try {
    // Usar fetchWithAuth para manejar tokens automáticamente
    const data = await fetchWithAuth(`${API_BASE_URL}/api/productosAprobacion/VerProductoCompleto/${productoId}`)
      .then(res => {
        if (!res.ok) throw new Error("No se pudo cargar el producto");
        return res.json();
      });

    console.log('ProductoCompletoDTO recibido:', data);

    // Colores
    window.productoState.coloresSeleccionados = Array.isArray(data.colores) ? [...data.colores] : [];

    // Archivos
    window.productoState.archivosSeleccionados = Array.isArray(data.archivos) && data.archivos.length > 0
      ? data.archivos.map(a => ({
          id: a.id,
          archivoImagen: a.archivoImagen,
          imagenPreview: a.archivoImagen ? `data:image/jpeg;base64,${a.archivoImagen}` : null,
          orden: a.orden
        }))
      : [];

    // Cargar producto en formulario y preview
    cargarProductoEnFormulario(data.producto, window.productoState.coloresSeleccionados, window.productoState.archivosSeleccionados);
    window.colorManager.actualizarListaColores();
    actualizarPreview();
    cargarProductoPreview(data.producto, window.productoState.coloresSeleccionados, window.productoState.archivosSeleccionados);

    // Mostrar botón de editar y guardar productoId
    btnEditar.style.display = "block";
    localStorage.setItem("productoId", productoId);

    // Cargar categorías y seleccionar la correspondiente
    await cargarCategoriasYSeleccionar(data.producto.categoriaId);

  } catch (error) {
    console.error("Error al cargar producto:", error);
    mostrarError("No se pudo cargar el producto: " + (error.message || "Error desconocido"));
  }
}

   
async function cargarCategoriasYSeleccionar(categoriaIdSeleccionada) {
  try {
    const res = await authManager.fetchWithAuth(`${API_BASE_URL}/api/categoria/combo`);
    if (!res.ok) throw new Error("No se pudieron cargar las categorías");

    const categorias = await res.json();

    // Actualizar <select>
    const select = document.getElementById("categoria");
    if (!select) return;
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

  // Previsualización de imágenes
  inputArchivos.addEventListener("change", (e) => {
    const archivosNuevos = Array.from(e.target.files);
    archivosNuevos.forEach(file => window.productoState.archivosSeleccionados.push(file));
    actualizarPreview();
    e.target.value = ""; // reset input
  });

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
  const categoriaId = parseInt(document.getElementById("categoria").value, 10);

  if (!nombre || isNaN(precio) || isNaN(categoriaId)) {
    alert("Completa los campos obligatorios correctamente.");
    return;
  }

  // Archivos existentes (base64/id) con propiedad eliminado
  const archivosExistentes = window.productoState.archivosSeleccionados
    .filter(a => !(a instanceof File))
    .map(a => ({
      id: a.id,
      archivoImagen: a.archivoImagen,
      orden: a.orden,
      eliminado: !!a.eliminado
    }));

  // Archivos nuevos tipo File
  const archivosNuevos = window.productoState.archivosSeleccionados.filter(a => a instanceof File);

  // DTO del producto
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
      tecnica: document.getElementById("tecnica").value.trim()
    },
    colores: window.productoState.coloresSeleccionados,
    archivos: archivosExistentes
  };

  // FormData
  const formData = new FormData();
  formData.append("producto", new Blob([JSON.stringify(productoCompletoDTO)], { type: "application/json" }));

  // Archivos nuevos
  archivosNuevos.forEach(file => formData.append("archivosNuevos", file));

  // Archivo comprimido
  const archivoComprimidoInput = document.getElementById("archivo-comprimido");
  if (archivoComprimidoInput && archivoComprimidoInput.files.length > 0) {
    formData.append("archivoComprimido", archivoComprimidoInput.files[0]);
  } else if (window.productoState.archivoComprimido === null) {
    formData.append("eliminarArchivoComprimido", "true");
  }

  try {
    // Usando authManager para manejar tokens
    const res = await authManager.fetchWithAuth(
      `${API_BASE_URL}/api/productosAprobacion/ActualizarProductoAprobar/${id}`,
      {
        method: "PUT",
        body: formData
      }
    );

    if (!res.ok) {
      let errorMessage = "Error actualizando producto";
      try {
        const errorData = await res.json();
        if (errorData.message) errorMessage = errorData.message;
      } catch {}
      throw new Error(errorMessage);
    }

    mostrarExito("Producto actualizado correctamente");

    // Refrescar productos y mantener selección
    await cargarProductos();
    selectedProductoId = parseInt(id, 10);

  } catch (error) {
    mostrarError("Error al actualizar producto: " + error.message);
    console.error(error);
  }
}


if (btnEditar) {
  btnEditar.addEventListener("click", e => {
    e.preventDefault();
    actualizarProducto();
  });
}




function cargarProductoPreview(producto, colores = [], archivos = []) {
  console.log("Cargando producto:", producto, colores, archivos);

  // --- Preview ---
  const setText = (id, value) => {
    const elem = document.getElementById(id);
    if (elem) elem.textContent = value ?? "-";
  };

  setText("product-title", producto.nombre);
  setText("product-description", producto.descripcion);
  setText("product-price", `$${(producto.precio || 0).toFixed(2)}`);
  setText("product-material", producto.material);
  setText("product-weight", producto.peso);
  setText("product-tecnica", producto.tecnica);
  setText(
    "product-dimensions",
    `${producto.dimensionAlto || "-"} x ${producto.dimensionAncho || "-"} x ${producto.dimensionProfundidad || "-"}`
  );

  // --- Formulario ---
  const setValue = (id, value) => {
    const elem = document.getElementById(id);
    if (elem) elem.value = value ?? "";
  };

  setValue("nombre", producto.nombre);
  setValue("descripcion", producto.descripcion);
  setValue("precio", producto.precio);

  // --- Imágenes ---
  const mainImage = document.getElementById("main-product-image");
  const miniaturasDiv = document.getElementById("image-thumbnails");
  if (miniaturasDiv) miniaturasDiv.innerHTML = "";

  const imgs = archivos.filter(a => a instanceof File || a.archivoImagen || a.linkArchivo || a.url);

  if (imgs.length && mainImage) {
    const primeraSrc = imgs[0] instanceof File
      ? URL.createObjectURL(imgs[0])
      : `data:image/png;base64,${imgs[0].archivoImagen}`;
    mainImage.src = primeraSrc;

    imgs.forEach((archivo, i) => {
      const src = archivo instanceof File
        ? URL.createObjectURL(archivo)
        : `data:image/png;base64,${archivo.archivoImagen}`;

      const thumb = document.createElement("div");
      thumb.className = "thumbnail" + (i === 0 ? " active" : "");
      const img = document.createElement("img");
      img.src = src;
      thumb.appendChild(img);

      thumb.addEventListener("click", () => {
        mainImage.src = src;
        document.querySelectorAll(".thumbnail").forEach(t => t.classList.remove("active"));
        thumb.classList.add("active");
      });

      miniaturasDiv.appendChild(thumb);
    });
  } else if (mainImage) {
    mainImage.src = "ruta_default.jpg";
  }

  // --- Colores ---
  const colorSelectorDiv = document.getElementById("color-selector"); // contenedor completo
  const colorDiv = document.getElementById("color-options"); // div donde van los colores

  if (colorDiv) {
    colorDiv.innerHTML = "";
    colores.forEach((colorObj, i) => {
      const color = colorObj.hex || colorObj;
      const div = document.createElement("div");
      div.className = "color-option" + (i === 0 ? " active" : "");
      div.style.backgroundColor = color;
      div.title = colorObj.nombre || color;
      div.addEventListener("click", () => {
        document.querySelectorAll(".color-option").forEach(c => c.classList.remove("active"));
        div.classList.add("active");
      });
      colorDiv.appendChild(div);
    });
  }

  // --- Formatos ---
  const formatButtons = document.querySelectorAll(".format-option");

  const actualizarColores = (mostrar) => {
    if (colorSelectorDiv) colorSelectorDiv.style.display = mostrar ? "flex" : "none";
  };

  formatButtons.forEach(btn => {
    btn.addEventListener("click", () => {
      formatButtons.forEach(b => b.classList.remove("active"));
      btn.classList.add("active");

      const precioElemento = document.getElementById("product-price");

      if (btn.dataset.format === "digital" && producto.precioDigital != null) {
        if (precioElemento) precioElemento.textContent = `$${producto.precioDigital.toFixed(2)}`;
        if (document.getElementById("precio")) document.getElementById("precio").value = producto.precioDigital;

        actualizarColores(false); // ocultar colores
      } else {
        if (precioElemento) precioElemento.textContent = `$${(producto.precio || 0).toFixed(2)}`;
        if (document.getElementById("precio")) document.getElementById("precio").value = producto.precio;

        actualizarColores(true); // mostrar colores
      }
    });
  });

  // --- Si el producto ya es digital al cargar ---
  if (producto.formato === "digital") {
    actualizarColores(false);
  } else {
    actualizarColores(true);
  }
}


async function eliminarProducto(id) {
  // Usar el modal de confirmación
  const confirmacion = await new Promise((resolve) => {
    mostrarConfirmacion("¿Seguro que querés eliminar este producto?", resolve);
  });

  if (!confirmacion) return;

  try {
    const res = await authManager.fetchWithAuth(
      `${API_BASE_URL}/api/productosAprobacion/BorrarProducto?id=${id}`,
      { method: "DELETE" }
    );

    if (!res.ok) {
      let errorMessage = "Error al eliminar producto";
      try {
        const data = await res.json();
        if (data.message) errorMessage = data.message;
      } catch {}
      mostrarError(errorMessage);
      return;
    }

    mostrarExito("Producto eliminado correctamente");
    cargarProductos(); // refrescar tabla
  } catch (error) {
    mostrarError("Error: " + error.message);
  }
}



});
