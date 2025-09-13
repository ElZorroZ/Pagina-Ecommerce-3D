window.productoState = window.productoState || {
  coloresSeleccionados: [],
  archivosSeleccionados: []
};
<<<<<<< HEAD

=======
>>>>>>> parent of 391f6a9 (Merge branch 'main' of https://github.com/ElZorroZ/Pagina-Ecommerce-3D)
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

<<<<<<< HEAD
=======
// Función para refrescar token (la dejé igual)
async function refreshAccessToken() {
  const refreshToken = localStorage.getItem("refreshToken");
  if (!refreshToken) {
    console.warn("No hay refresh token guardado");
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

>>>>>>> parent of 391f6a9 (Merge branch 'main' of https://github.com/ElZorroZ/Pagina-Ecommerce-3D)
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
<<<<<<< HEAD
    const res = await authManager.fetchWithAuth(`${API_BASE_URL}/api/productosAprobacion/VerProductos`);
    if (!res.ok) throw new Error("Error al obtener los productos");
    const productos = await res.json();
=======
    const response = await fetchConRefresh("http://localhost:8080/api/productosAprobacion/VerProductos");
    if (!response.ok) throw new Error("Error al obtener los productos");

    const productos = await response.json();
>>>>>>> parent of 391f6a9 (Merge branch 'main' of https://github.com/ElZorroZ/Pagina-Ecommerce-3D)
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
<<<<<<< HEAD
      const res = await authManager.fetchWithAuth(`${API_BASE_URL}/api/productosAprobacion/VerProductoCompleto/${productoId}`);
      if (!res.ok) throw new Error("No se pudo cargar el producto");
      const data = await res.json();
      if (!data) return mostrarError("Producto no encontrado");
=======
        const token = localStorage.getItem("accessToken");
        const res = await fetch(`http://localhost:8080/api/productosAprobacion/VerProductoCompleto/${productoId}`, {
            headers: { "Authorization": `Bearer ${token}` }
        });
>>>>>>> parent of 391f6a9 (Merge branch 'main' of https://github.com/ElZorroZ/Pagina-Ecommerce-3D)

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
<<<<<<< HEAD
      const res = await authManager.fetchWithAuth(`${API_BASE_URL}/api/categoria/combo`);
=======
      const token = localStorage.getItem("accessToken");
      const res = await fetch("http://localhost:8080/api/categoria/combo", {
        headers: { "Authorization": `Bearer ${token}` }
      });
>>>>>>> parent of 391f6a9 (Merge branch 'main' of https://github.com/ElZorroZ/Pagina-Ecommerce-3D)
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

<<<<<<< HEAD
  formatButtons.forEach(btn => {
    btn.addEventListener("click", () => {
      formatButtons.forEach(b => b.classList.remove("active"));
      btn.classList.add("active");
=======
    const url = new URL("http://localhost:8080/api/productosAprobacion/BorrarProducto");
    url.searchParams.append("id", id);
>>>>>>> parent of 391f6a9 (Merge branch 'main' of https://github.com/ElZorroZ/Pagina-Ecommerce-3D)

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
