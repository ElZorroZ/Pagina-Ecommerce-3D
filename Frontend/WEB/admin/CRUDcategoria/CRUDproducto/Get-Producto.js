// Estado global para colores y archivos
window.productoState = window.productoState || {
  coloresSeleccionados: [],
  archivosSeleccionados: []
};

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

document.addEventListener("DOMContentLoaded", () => {
  const tablaBody = document.getElementById("tabla-productos");
  const listaColores = document.getElementById("lista-colores");
  const btnAgregarColor = document.getElementById("btn-agregar-color");
  const inputColor = document.getElementById("input-color");
  const btnEditar = document.getElementById("btn-editar-producto");
  const preview = document.getElementById("preview-imagenes");
   const inputArchivos = document.getElementById("imagenes");
    console.log("inputColor:", inputColor);

  inputArchivos.addEventListener("change", (e) => {
    const archivosNuevos = Array.from(e.target.files);
    archivosNuevos.forEach(file => window.productoState.archivosSeleccionados.push(file));
    actualizarPreview();
    e.target.value = ""; // para poder subir más archivos luego
  });
  console.log("btnAgregarColor:", btnAgregarColor);

  btnAgregarColor.addEventListener("click", () => {
    console.log("Click detectado en btnAgregarColor");
    const color = inputColor.value;
    console.log("Valor inputColor:", `"${color}"`);
    const colorTrim = color.trim();
    console.log("Valor inputColor.trim():", `"${colorTrim}"`);

    if (colorTrim && !window.productoState.coloresSeleccionados.includes(colorTrim)) {
      window.productoState.coloresSeleccionados.push(colorTrim);
      console.log("Colores seleccionados:", window.productoState.coloresSeleccionados);
      actualizarListaColores();
      inputColor.value = "";
      inputColor.focus();
    } else {
      console.log("No se agregó el color. O estaba vacío o ya existe.");
    }
  });
  // Cargar productos y llenar tabla
  async function cargarProductos() {
    try {
      const response = await fetchConRefresh("http://localhost:8080/api/productos");
      if (!response.ok) throw new Error("Error al obtener los productos");
      const productos = await response.json();
      tablaBody.innerHTML = "";
      productos.forEach(producto => {
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

  cargarProductos();

  // Seleccionar producto y cargar en formulario + preview
  async function selectProducto(productoId) {
    try {
      const token = localStorage.getItem("accessToken");
      const res = await fetch(`http://localhost:8080/api/productos/${productoId}`, {
        headers: { "Authorization": `Bearer ${token}` }
      });
      if (!res.ok) throw new Error("No se pudo cargar el producto");
      const data = await res.json();
      console.log('ProductoCompletoDTO recibido:', data);
      window.productoState.coloresSeleccionados = Array.isArray(data.colores) && data.colores.length > 0
        ? [...data.colores]
        : window.productoState.coloresSeleccionados;

      window.productoState.archivosSeleccionados = Array.isArray(data.archivos) && data.archivos.length > 0
        ? data.archivos.map(a => ({
            id: a.id,
            linkArchivo: a.linkArchivo || a.url,
            orden: a.orden
          }))
        : window.productoState.archivosSeleccionados;

      cargarProductoEnFormulario(data.producto, window.productoState.coloresSeleccionados, window.productoState.archivosSeleccionados);
      actualizarListaColores();
      actualizarPreview();

      btnEditar.style.display = "block";
      localStorage.setItem("productoId", productoId);
    } catch (error) {
      console.error(error);
      alert("Error al cargar producto");
    }
  }

  // Actualizar lista colores en UI
  function actualizarListaColores() {
    listaColores.innerHTML = "";
    window.productoState.coloresSeleccionados.forEach((color, index) => {
      const li = document.createElement("li");
      li.textContent = color + " ";
      const btnBorrar = document.createElement("button");
      btnBorrar.textContent = "x";
      btnBorrar.style.marginLeft = "8px";
      btnBorrar.addEventListener("click", () => {
        window.productoState.coloresSeleccionados.splice(index, 1);
        actualizarListaColores();
      });
      li.appendChild(btnBorrar);
      listaColores.appendChild(li);
    });
  }
   


  // Preview archivos
  function actualizarPreview() {
    preview.innerHTML = "";
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

  // Carga producto en formulario
  function cargarProductoEnFormulario(producto, colores, archivos) {
    document.getElementById("producto-id").value = producto.id || "";
    document.getElementById("nombre").value = producto.nombre || "";
    document.getElementById("descripcion").value = producto.descripcion || "";
    document.getElementById("precio").value = producto.precio || "";
  }

  // Función para actualizar producto
  async function actualizarProducto() {
    const id = document.getElementById("producto-id").value;
    const nombre = document.getElementById("nombre").value.trim();
    const descripcion = document.getElementById("descripcion").value.trim();
    const precio = parseFloat(document.getElementById("precio").value);

    if (!nombre || isNaN(precio)) {
      alert("Completa los campos obligatorios correctamente.");
      return;
    }
    const archivosExistentes = window.productoState.archivosSeleccionados
    .filter(a => !(a instanceof File))
    .map(a => ({
      id: a.id,
      linkArchivo: a.linkArchivo || a.url
    }));

    const archivosNuevos = window.productoState.archivosSeleccionados
      .filter(a => a instanceof File);

    const productoCompletoDTO = {
      producto: {
        nombre,
        descripcion,
        precio,
      },
      colores: window.productoState.coloresSeleccionados,
      archivos: archivosExistentes  // acá solo los links o IDs
    };

    // armar FormData
    const formData = new FormData();
    formData.append(
      "producto",
      new Blob([JSON.stringify(productoCompletoDTO)], { type: "application/json" })
    );

    archivosNuevos.forEach(file => {
      formData.append("archivosNuevos", file);
    });

    try {
      console.log("Colores antes de enviar:", window.productoState.coloresSeleccionados);
      console.log("Archivos antes de enviar:", window.productoState.archivosSeleccionados);

      for (const pair of formData.entries()) {
        console.log(pair[0], pair[1]);
      }
      console.log("DTO que envío:", productoCompletoDTO);
      const res = await fetchConRefresh(`http://localhost:8080/api/productos/${id}`, {
        method: "PUT",
        body: formData,
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
      // Opcional: recargar lista o limpiar formulario
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
  document.getElementById("prev-nombre").textContent = producto.nombre;
  document.getElementById("prev-desc").textContent = producto.descripcion;
  document.getElementById("prev-precio").textContent = `$${producto.precio.toFixed(2)}`;
  console.log("Archivos:", archivos);

  const mainImage = document.getElementById("main-image");
  if (archivos.length > 0) {
    mainImage.src = archivos[0].linkArchivo || "ruta_default.jpg";
  } else {
    mainImage.src = "ruta_default.jpg";
  }

  const miniaturasDiv = document.getElementById("miniaturas");
  miniaturasDiv.innerHTML = "";

  console.log("Archivos para miniaturas:", archivos);

  archivos.forEach((archivo) => {
    const src = archivo.linkArchivo || "ruta_default.jpg"; // Usamos linkArchivo según tu JSON
    const thumb = document.createElement("img");
    thumb.src = src;
    thumb.className = "thumbnail-image";
    thumb.onclick = () => mainImage.src = thumb.src;
    miniaturasDiv.appendChild(thumb);
  });


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
    const res = await fetch(`http://localhost:8080/api/productos/${id}`, {
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
