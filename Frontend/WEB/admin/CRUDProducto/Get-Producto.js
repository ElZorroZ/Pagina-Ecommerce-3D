// Estado global para colores y archivos
window.productoState = window.productoState || {
  coloresSeleccionados: [],
  archivosSeleccionados: []
};
let preview;

  // Preview archivos
  function actualizarPreview() {
    preview.innerHTML = "";
    if (!window.productoState.archivosSeleccionados || window.productoState.archivosSeleccionados.length === 0) {
      return; // No hay archivos, no mostramos nada
    }
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
          window.productoState.archivosSeleccionados.splice(idx, 1);
          actualizarPreview();
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
    window.productoState.archivoComprimido = null;
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
  const btnAgregarColor = document.getElementById("btn-agregar-color");
  const inputColorNombre = document.getElementById("input-color-nombre");
  const btnEditar = document.getElementById("btn-editar-producto");
  preview = document.getElementById("preview-imagenes");
   const inputArchivos = document.getElementById("imagenes");
  const categoriesDropdown = document.querySelector("#categories-dropdown .dropdown-content");
  const shopTrigger = document.getElementById("shop-trigger");
  
 // Render categories in dropdown
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
// Estado para producto seleccionado
let productoSeleccionadoId = null;

// --- Cargar productos y llenar tabla ---
async function cargarProductos() {
  try {
    mostrarCarga("Cargando productos..."); // Mostrar overlay
    const response = await authManager.fetchWithAuth(`${API_BASE_URL}/api/productos`);
    if (!response.ok) throw new Error("Error al obtener los productos");
    const productos = await response.json();

    tablaBody.innerHTML = "";

    productos.forEach(producto => {
      const fila = document.createElement("tr");
      const estrella = producto.destacado ? "⭐" : "☆";

      fila.innerHTML = `
        <td>${producto.id}</td>
        <td>${producto.nombre}</td>
        <td>${producto.descripcion}</td>
        <td>$${producto.precio.toFixed(2)}</td>
        <td>
          <button class="select">Seleccionar</button>
          <button class="eliminar">Eliminar</button>
          <button class="estrella">${estrella}</button>
        </td>
      `;

      const btnSelect = fila.querySelector(".select");

      // Mostrar/ocultar según producto seleccionado
      btnSelect.style.display = (producto.id === productoSeleccionadoId) ? "none" : "inline-block";

      // Evento seleccionar
      btnSelect.addEventListener("click", () => {
        productoSeleccionadoId = producto.id;
        cargarProductos(); // recargar tabla para actualizar botones
        mostrarExito(`Producto "${producto.nombre}" seleccionado.`);
        selectProducto(producto.id);
      });

      // Evento eliminar
      fila.querySelector(".eliminar").addEventListener("click", () => {
        eliminarProducto(producto.id)
          .then(() => {
            // Si querés, podés refrescar tabla o mostrar un mensaje extra aquí
            cargarProductos();
          });
      });


      // Evento estrella
      fila.querySelector(".estrella").addEventListener("click", () => {
        const yaEsDestacado = producto.destacado;
        if (!yaEsDestacado) {
          const destacadosActuales = [...document.querySelectorAll(".estrella")]
            .filter(btn => btn.textContent === "⭐").length;

          if (destacadosActuales >= 10) {
            mostrarError("No se pueden destacar más de 10 productos.");
            return;
          }
        }
        toggleDestacado(producto.id);
      });

      tablaBody.appendChild(fila);
    });

  } catch (error) {
    console.error("Error al cargar productos:", error);
    mostrarError("No se pudieron cargar los productos");
  }finally {
        ocultarCarga(); // Ocultar overlay siempre
  }
}


  // Hacemos pública la función por si la necesitás desde otro script
  window.cargarProductos = cargarProductos;

  // Primera carga
  cargarProductos();
// Cambiar estado de destacado
async function toggleDestacado(productoId) {
  try {
    mostrarCarga("Destacando el producto..."); // Mostrar overlay
    const res = await authManager.fetchWithAuth(`${API_BASE_URL}/api/productos/${productoId}/destacado`, {
      method: "POST" // o PUT según tu API
    });

    if (!res.ok) {
      const errorText = await res.text();
      throw new Error(errorText || "No se pudo cambiar el estado de destacado");
    }

    mostrarExito("El estado de destacado se actualizó correctamente.");
    await cargarProductos(); // refresca la tabla
  } catch (error) {
    console.error(error);
    mostrarError("Error al actualizar destacado: " + error.message);
  }finally {
        ocultarCarga(); // Ocultar overlay siempre
    }
}

// Seleccionar producto y cargar en formulario + preview
async function selectProducto(productoId) {
  try {
    mostrarCarga("Seleccionando producto..."); // Mostrar overlay
    const res = await authManager.fetchWithAuth(`${API_BASE_URL}/api/productos/${productoId}`);
    if (!res.ok) throw new Error("No se pudo cargar el producto");

    const data = await res.json();
    console.log('ProductoCompletoDTO recibido:', data);

    // Actualizar producto seleccionado
    productoSeleccionadoId = productoId;
    cargarProductos(); // refresca la tabla para ocultar/mostrar botones "Seleccionar"

    // Colores
    window.productoState.coloresSeleccionados = Array.isArray(data.colores)
      ? [...data.colores]
      : [];

    // Archivos
    window.productoState.archivosSeleccionados = Array.isArray(data.archivos) && data.archivos.length > 0
      ? data.archivos.map(a => ({
          id: a.id,
          linkArchivo: a.linkArchivo || a.url,
          orden: a.orden
        }))
      : [];

    // Cargar en formulario y preview
    cargarProductoEnFormulario(
      data.producto,
      window.productoState.coloresSeleccionados,
      window.productoState.archivosSeleccionados
    );
    window.colorManager.actualizarListaColores();
    actualizarPreview();
    cargarProductoPreview(
      data.producto,
      window.productoState.coloresSeleccionados,
      window.productoState.archivosSeleccionados
    );

    // Mostrar botón editar
    btnEditar.style.display = "block";

    // Guardar ID localmente
    localStorage.setItem("productoId", productoId);

    // Seleccionar categoría en el dropdown
    await cargarCategoriasYSeleccionar(data.producto.categoriaId);


  } catch (error) {
    console.error(error);
    mostrarError("Error al cargar el producto: " + error.message);
  }finally {
        ocultarCarga(); // Ocultar overlay siempre
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


 // Carga producto en formulario
function cargarProductoEnFormulario(producto, colores, archivos) {
  
  document.getElementById("producto-id").value = producto.id || "";
  document.getElementById("nombre").value = producto.nombre || "";
  document.getElementById("descripcion").value = producto.descripcion || "";
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

  // STL preview (si querés mostrarlo como enlace para descargar)
   // STL preview (si querés mostrarlo como enlace para descargar)
    const preview = document.getElementById("comprimido-preview");
    if (producto.archivoComprimido) {
      console.log("Base64 recibido:", producto.archivoComprimido);
      mostrarArchivoComprimido(producto.archivoComprimido);
    } else if (preview) {
      preview.innerHTML = "";
    }

}
// Función para actualizar producto
async function actualizarProducto() {
  const id = document.getElementById("producto-id").value;
  const nombre = document.getElementById("nombre").value.trim();
  const descripcion = document.getElementById("descripcion").value.trim();
  const precio = parseFloat(document.getElementById("precio").value);
  const categoriaId = parseInt(document.getElementById("categoria").value);

  if (!nombre || isNaN(precio)) {
    mostrarError("Completa los campos obligatorios correctamente.");
    return;
  }

  // Archivos existentes (mantener)
  const archivosExistentes = window.productoState.archivosSeleccionados
    .filter(a => !(a instanceof File))
    .map(a => ({
      id: a.id,
      linkArchivo: a.linkArchivo || a.url
    }));

  // Archivos nuevos (File)
  const archivosNuevos = window.productoState.archivosSeleccionados
    .filter(a => a instanceof File);

  // Validar que no se estén intentando usar imágenes existentes de otro producto
  const hayArchivosInvalidos = archivosNuevos.some(file => file.id);
  if (hayArchivosInvalidos) {
    mostrarError("No es posible subir imágenes de otro producto. Elimina las imágenes inválidas.");
    return;
  }
  const precioDigital=0;


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
      archivoComprimido: null, // explícitamente null para evitar confusión
    },
    colores: window.productoState.coloresSeleccionados,
    archivos: archivosExistentes
  };

  // Armar FormData
  const formData = new FormData();
  formData.append(
    "producto",
    new Blob([JSON.stringify(productoCompletoDTO)], { type: "application/json" })
  );

  // Nuevos archivos
  archivosNuevos.forEach(file => {
    formData.append("archivosNuevos", file);
  });

  // Archivo comprimido (si hay)
  const archivoComprimidoInput = document.getElementById("archivo-comprimido");
  if (archivoComprimidoInput && archivoComprimidoInput.files.length > 0) {
    formData.append("archivoComprimido", archivoComprimidoInput.files[0]);
  }

  try {
    mostrarCarga("Actualizando producto..."); // Mostrar overlay
    console.log("Enviando DTO:", productoCompletoDTO);

    const res = await authManager.fetchWithAuth(`${API_BASE_URL}/api/productos/${id}`, {
      method: "PUT",
      body: formData
    });

    if (!res.ok) {
      let errorMessage = "Error actualizando producto";
      try {
        const errorData = await res.json();
        if (errorData.message) errorMessage = errorData.message;
      } catch {}
      throw new Error(errorMessage);
    }

    mostrarExito("Producto actualizado correctamente");
    cargarProductos();

    // Reset de estado/formulario
    const form = document.getElementById("form-producto");
    if (form) form.reset();
    window.productoState.coloresSeleccionados = [];
    window.productoState.archivosSeleccionados = [];
    window.colorManager.actualizarListaColores();
    actualizarPreview();
    actualizarPreviewComprimido();

  } catch (error) {
    mostrarError("❌ Error: " + error.message);
  }finally {
        ocultarCarga(); // Ocultar overlay siempre
    }
}


// Bind botón editar
if (btnEditar) {
  btnEditar.addEventListener("click", e => {
    e.preventDefault();
    actualizarProducto();
  });
}

function cargarProductoPreview(producto, colores = [], archivos = []) {
  console.log("Cargando producto:", producto, colores, archivos);

  // --- Preview ---
  document.getElementById("product-title").textContent = producto.nombre || "-";
  document.getElementById("product-description").textContent = producto.descripcion || "-";
  document.getElementById("product-material").textContent = producto.material || "-";
  document.getElementById("product-weight").textContent = producto.peso || "-";
  document.getElementById("product-tecnica").textContent = producto.tecnica || "-";
  document.getElementById("product-dimensions").textContent =
    `${producto.dimensionAlto || "-"} x ${producto.dimensionAncho || "-"} x ${producto.dimensionProfundidad || "-"}`;

  // --- Formulario ---
  if (document.getElementById("nombre")) document.getElementById("nombre").value = producto.nombre || "";
  if (document.getElementById("descripcion")) document.getElementById("descripcion").value = producto.descripcion || "";
  if (document.getElementById("precio")) document.getElementById("precio").value = producto.precio || "";

  // --- Imágenes ---
  const mainImage = document.getElementById("main-product-image");
  const miniaturasDiv = document.getElementById("image-thumbnails");
  if (miniaturasDiv) miniaturasDiv.innerHTML = "";

  const imgs = archivos.filter(a => a instanceof File || a.linkArchivo || a.url);
  if (imgs.length && mainImage) {
    const primeraSrc = imgs[0] instanceof File ? URL.createObjectURL(imgs[0]) : imgs[0].linkArchivo || imgs[0].url;
    mainImage.src = primeraSrc;

    imgs.forEach((archivo, i) => {
      const src = archivo instanceof File ? URL.createObjectURL(archivo) : archivo.linkArchivo || archivo.url;
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
        document.querySelectorAll(".color-option").forEach(c => c.classList.remove("active"));
        div.classList.add("active");
      });
      colorDiv.appendChild(div);
    });
  }

  // --- Precio (solo físico) ---
  const precioElemento = document.getElementById("product-price");
  if (precioElemento) precioElemento.textContent = `$${(producto.precio || 0).toFixed(2)}`;
}




async function eliminarProducto(id) {
  // Usar confirmación visual
  mostrarConfirmacion("¿Seguro que querés eliminar este producto?", async (confirmado) => {
    if (!confirmado) return;

    try {
      mostrarCarga("Eliminando producto..."); // Mostrar overlay
      const res = await authManager.fetchWithAuth(`${API_BASE_URL}/api/productos/${id}`, {
        method: "DELETE"
      });

      if (!res.ok) {
        let errorMessage = "Error al eliminar producto";

        try {
          const errorData = await res.json();
          if (errorData.message) errorMessage = errorData.message;
        } catch {
          const text = await res.text();
          if (text) {
            const match = /:\s*(.*)/.exec(text);
            if (match && match[1]) errorMessage = match[1];
            else errorMessage = text;
          }
        }

        throw new Error(errorMessage);
      }

      // Éxito
      mostrarExito(`Producto eliminado correctamente`);

      // Limpiar formulario
      const form = document.getElementById("form-producto");
      if (form) form.reset();

      // Limpiar estado global
      window.productoState.coloresSeleccionados = [];
      window.productoState.archivosSeleccionados = [];
      window.productoState.archivoComprimido = null;
      window.colorManager.actualizarListaColores();
      window.actualizarPreview();
      window.actualizarPreviewComprimido();

      // Refrescar tabla
      cargarProductos();

    } catch (error) {
      mostrarError("Error: " + error.message);
    }finally {
        ocultarCarga(); // Ocultar overlay siempre
    }
  });
}
});
