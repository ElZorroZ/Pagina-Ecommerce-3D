window.productoState = window.productoState || {
  coloresSeleccionados: [],
  archivosSeleccionados: []
};

let preview;

// --- Funciones de preview ---
function actualizarPreview() {
  if (!preview) return;
  preview.innerHTML = "";
  if (!window.productoState.archivosSeleccionados || window.productoState.archivosSeleccionados.length === 0) return;

  window.productoState.archivosSeleccionados.forEach(archivo => {
    const div = document.createElement("div");
    Object.assign(div.style, { position: "relative", display: "inline-block", marginRight: "10px" });

    const img = document.createElement("img");
    Object.assign(img.style, { width: "80px", height: "80px", objectFit: "cover", border: "1px solid #ccc", borderRadius: "4px" });

    if (archivo instanceof File) {
      const reader = new FileReader();
      reader.onload = e => img.src = e.target.result;
      reader.readAsDataURL(archivo);
    } else {
      img.src = archivo.linkArchivo || archivo.url || "ruta_default.jpg";
    }

    div.appendChild(img);
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

function mostrarArchivoComprimido(base64, nombre = 'archivo.zip') {
  if (!base64) return;

  const byteCharacters = atob(base64);
  const byteNumbers = Array.from(byteCharacters).map(c => c.charCodeAt(0));
  const byteArray = new Uint8Array(byteNumbers);

  const blob = new Blob([byteArray], { type: 'application/octet-stream' });
  const url = URL.createObjectURL(blob);

  const previewComp = document.getElementById('comprimido-preview');
  previewComp.innerHTML = '';

  const link = document.createElement('a');
  link.href = url;
  link.download = nombre;
  link.textContent = `Descargar ${nombre}`;
  link.style.display = 'inline-block';
  link.style.marginRight = '10px';

  const btnEliminar = document.createElement('button');
  btnEliminar.textContent = 'X';
  btnEliminar.title = 'Eliminar archivo comprimido';
  Object.assign(btnEliminar.style, { background: 'rgba(255,0,0,0.7)', color: 'white', border: 'none', cursor: 'pointer', borderRadius: '4px', padding: '0 6px' });

  btnEliminar.addEventListener('click', () => {
    window.productoState.archivoComprimido = null;
    document.getElementById('archivo-comprimido').value = "";
    previewComp.innerHTML = "";
    URL.revokeObjectURL(url);
  });

  previewComp.appendChild(link);
  previewComp.appendChild(btnEliminar);
}

document.addEventListener("DOMContentLoaded", () => {
  const tablaBody = document.getElementById("tabla-productos");
  const listaColores = document.getElementById("lista-colores");
  preview = document.getElementById("preview-imagenes");
  const categoriesDropdown = document.querySelector("#categories-dropdown .dropdown-content");
  const shopTrigger = document.getElementById("shop-trigger");
  // --- Cargar productos ---
  async function cargarProductos() {
  try {
    const res = await authManager.fetchWithAuth(`${API_BASE_URL}/api/productosAprobacion/VerProductos`);
    if (!res.ok) throw new Error("Error al obtener los productos");
    const productos = await res.json();
    tablaBody.innerHTML = "";

    productos.forEach(wrapper => {
      const producto = wrapper.producto;
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

      const btnSelect = fila.querySelector(".select");
      const btnEliminar = fila.querySelector(".eliminar");

      // ocultar botón Seleccionar si es el producto seleccionado
      if (window.productoState?.productoSeleccionadoId === producto.id) {
        btnSelect.style.display = "none";
      } else {
        btnSelect.style.display = "inline-block";
      }

      btnSelect.addEventListener("click", () => {
        // marcar producto como seleccionado
        window.productoState = window.productoState || {};
        window.productoState.productoSeleccionadoId = producto.id;
        // recargar la tabla para actualizar visibilidad de botones
        cargarProductos();
        selectProducto(producto.id); // tu lógica adicional
        mostrarExito(`Producto "${producto.nombre}" seleccionado.`);
      });

      btnEliminar.addEventListener("click", () => eliminarProducto(producto.id));

      tablaBody.appendChild(fila);
    });
  } catch (error) {
    console.error("Error al cargar productos:", error.message);
    mostrarError("No se pudieron cargar los productos");
  }
}

  window.cargarProductos = cargarProductos;
  cargarProductos();

  // --- Seleccionar producto ---
  async function selectProducto(productoId) {
    try {
      const res = await authManager.fetchWithAuth(`${API_BASE_URL}/api/productosAprobacion/VerProductoCompleto/${productoId}`);
      if (!res.ok) throw new Error("No se pudo cargar el producto");
      const data = await res.json();
      if (!data) return mostrarError("Producto no encontrado");

      console.log('ProductoCompletoAprobacionDTO recibido:', data);

      // Colores
      window.productoState.coloresSeleccionados = Array.isArray(data.colores) ? [...data.colores] : [];
      // Archivos
      window.productoState.archivosSeleccionados = Array.isArray(data.archivos)
        ? data.archivos.map((a, index) => ({ id: a.id, orden: a.orden ?? index, linkArchivo: `data:image/png;base64,${a.archivoImagen}` }))
        : [];

      cargarProductoEnFormulario(data.producto, window.productoState.coloresSeleccionados, window.productoState.archivosSeleccionados);
          cargarProductoPreview(data.producto, window.productoState.coloresSeleccionados, window.productoState.archivosSeleccionados);
      actualizarListaColores();
      actualizarPreview();

      localStorage.setItem("productoId", productoId);
      await cargarCategoriasYSeleccionar(data.producto.categoriaId);

    } catch (error) {
      console.error(error);
      mostrarError("Error al cargar producto");
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

  // --- Lista de colores ---
  function actualizarListaColores() {
    listaColores.innerHTML = "";
    window.productoState.coloresSeleccionados.forEach(colorObj => {
      const li = document.createElement("li");
      Object.assign(li.style, { backgroundColor: colorObj.hex, color: "#fff", padding: "5px 10px", borderRadius: "4px", display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: "6px" });
      li.title = colorObj.nombre;
      const span = document.createElement("span");
      span.textContent = colorObj.hex;
      li.appendChild(span);
      listaColores.appendChild(li);
    });
  }
  window.actualizarListaColores = actualizarListaColores;
  actualizarListaColores();

  // --- Cargar categorías ---
  async function cargarCategoriasYSeleccionar(categoriaIdSeleccionada) {
    try {
      const res = await authManager.fetchWithAuth(`${API_BASE_URL}/api/categoria/combo`);
      if (!res.ok) throw new Error("No se pudieron cargar las categorías");
      const categorias = await res.json();

      const select = document.getElementById("categoria");
      select.innerHTML = '<option value="">Seleccionar categoría</option>';
      categorias.forEach(cat => {
        const option = document.createElement("option");
        option.value = cat.id;
        option.textContent = cat.nombre;
        if (cat.id === categoriaIdSeleccionada) option.selected = true;
        select.appendChild(option);
      });
    } catch (err) {
      mostrarError("Error cargando categorías: " + err.message);
    }
  }

  // --- Cargar producto en formulario ---
  function cargarProductoEnFormulario(producto, colores, archivos) {
    const setVal = (id, value) => document.getElementById(id).value = value ?? "";

    setVal("producto-id", producto.id);
    setVal("nombre", producto.nombre);
    setVal("descripcion", producto.descripcion);
    setVal("precio", producto.precio);
    setVal("precioDigital", producto.precioDigital);
    setVal("codigo-inicial", producto.codigoInicial);
    setVal("version", producto.version);
    setVal("seguimiento", producto.seguimiento);
    setVal("dimension-alto", producto.dimensionAlto);
    setVal("dimension-ancho", producto.dimensionAncho);
    setVal("dimension-profundidad", producto.dimensionProfundidad);
    setVal("material", producto.material);
    setVal("tecnica", producto.tecnica);
    setVal("peso", parseFloat(producto.peso?.toString().replace(/[^\d.]/g, "")) || "");

    if (producto.archivo) mostrarArchivoComprimido(producto.archivo);
    else document.getElementById('comprimido-preview').innerHTML = "";
  }
function cargarProductoPreview(producto, colores = [], archivos = []) {
  console.log("Cargando producto:", producto, colores, archivos);

  // --- Texto ---
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
  setValue("precioDigital", producto.precioDigital);

  // --- Imágenes ---
  const mainImage = document.getElementById("main-product-image");
  const miniaturasDiv = document.getElementById("image-thumbnails");
  if (miniaturasDiv) miniaturasDiv.innerHTML = "";

  const imgs = archivos.filter(a => a instanceof File || a.archivoImagen || a.linkArchivo || a.url);
  
  if (imgs.length && mainImage) {
    const primeraSrc = imgs[0] instanceof File
      ? URL.createObjectURL(imgs[0])
      : (imgs[0].linkArchivo || imgs[0].url || `data:image/png;base64,${imgs[0].archivoImagen}`);
    mainImage.src = primeraSrc;

    imgs.forEach((archivo, i) => {
      const src = archivo instanceof File
        ? URL.createObjectURL(archivo)
        : (archivo.linkArchivo || archivo.url || `data:image/png;base64,${archivo.archivoImagen}`);

      const thumb = document.createElement("div");
      thumb.className = "thumbnail" + (i === 0 ? " active" : "");

      const img = document.createElement("img");
      img.src = src;
      thumb.appendChild(img);

      thumb.addEventListener("click", () => {
        mainImage.src = src;
        miniaturasDiv.querySelectorAll(".thumbnail").forEach(t => t.classList.remove("active"));
        thumb.classList.add("active");
      });

      miniaturasDiv.appendChild(thumb);
    });
  } else if (mainImage) {
    mainImage.src = "ruta_default.jpg";
  }

  // --- Colores ---
  const colorSelectorDiv = document.getElementById("color-selector");
  const colorDiv = document.getElementById("color-options");

  if (colorDiv) {
    colorDiv.innerHTML = "";
    colores.forEach((colorObj, i) => {
      const color = colorObj.hex || colorObj;
      const div = document.createElement("div");
      div.className = "color-option" + (i === 0 ? " active" : "");
      div.style.backgroundColor = color;
      div.title = colorObj.nombre || color;
      div.addEventListener("click", () => {
        colorDiv.querySelectorAll(".color-option").forEach(c => c.classList.remove("active"));
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
        actualizarColores(false);
      } else {
        if (precioElemento) precioElemento.textContent = `$${(producto.precio || 0).toFixed(2)}`;
        if (document.getElementById("precio")) document.getElementById("precio").value = producto.precio;
        actualizarColores(true);
      }
    });
  });

  // --- Ajuste inicial según formato ---
  if (producto.formato === "digital") actualizarColores(false);
  else actualizarColores(true);
}

 // --- Eliminar producto ---
async function eliminarProducto(id) {
  mostrarConfirmacion("¿Seguro que querés eliminar este producto?", async (confirmado) => {
    if (!confirmado) return;

    try {
      const res = await authManager.fetchWithAuth(
        `${API_BASE_URL}/api/productosAprobacion/BorrarProducto?id=${id}`,
        { method: "DELETE" }
      );

      if (!res.ok) {
        const errorText = await res.text();
        mostrarError(errorText || "Error al eliminar producto");
        throw new Error(errorText || "Error al eliminar producto");
      }

      mostrarExito("Producto eliminado correctamente");
      // Limpiar selección si se eliminó el producto seleccionado
      if (window.productoState?.productoSeleccionadoId === id) {
        window.productoState.productoSeleccionadoId = null;
      }
      cargarProductos();
    } catch (error) {
      mostrarError("Error: " + error.message);
      console.error(error);
    }
  });
}

});
