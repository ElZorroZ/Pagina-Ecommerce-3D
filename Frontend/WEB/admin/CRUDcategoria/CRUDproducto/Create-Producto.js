 () => {
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


const inputImagenes = document.getElementById('imagenes');
const preview = document.getElementById('preview-imagenes');
let archivosSeleccionados = [];

inputImagenes.addEventListener('change', () => {
  const files = Array.from(inputImagenes.files);

  files.forEach(file => {
    if (archivosSeleccionados.length < 5) {
      const existe = archivosSeleccionados.some(f => f.name === file.name && f.size === file.size);
      if (!existe) archivosSeleccionados.push(file);
    }
  });

  actualizarPreview();
  inputImagenes.value = "";
});

function actualizarPreview() {
  preview.innerHTML = "";

  archivosSeleccionados.forEach((file, idx) => {
    const reader = new FileReader();
    reader.onload = (e) => {
      const div = document.createElement("div");
      div.style.position = "relative";
      div.style.display = "inline-block";
      div.style.marginRight = "10px";

      const img = document.createElement("img");
      img.src = e.target.result;
      img.style.width = "80px";
      img.style.height = "80px";
      img.style.objectFit = "cover";
      img.style.border = "1px solid #ccc";
      img.style.borderRadius = "4px";
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
        archivosSeleccionados.splice(idx, 1);
        actualizarPreview();
      });

      div.appendChild(btnEliminar);
      preview.appendChild(div);
    };
    reader.readAsDataURL(file);
  });
}

async function subirArchivoBackend(productoId, file, orden) {
  const formData = new FormData();
  formData.append("file", file);
  formData.append("orden", orden);

  const res = await fetchConRefresh(`http://localhost:8080/api/productos/${productoId}/archivos`, {
    method: "POST",
    body: formData,
  });

  if (!res.ok) {
    const errorData = await res.json();
    throw new Error(errorData.message || "Error subiendo archivo al backend");
  }
  return await res.json();
}

document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("form-producto");
  const inputColor = document.getElementById("input-color");
  const btnAgregarColor = document.getElementById("btn-agregar-color");
  const listaColores = document.getElementById("lista-colores");

  let colores = [];

  function actualizarListaColores() {
    listaColores.innerHTML = "";
    colores.forEach((color, index) => {
      const li = document.createElement("li");
      li.textContent = color + " ";

      const btnBorrar = document.createElement("button");
      btnBorrar.textContent = "x";
      btnBorrar.style.marginLeft = "8px";
      btnBorrar.addEventListener("click", () => {
        colores.splice(index, 1);
        actualizarListaColores();
      });

      li.appendChild(btnBorrar);
      listaColores.appendChild(li);
    });
  }

  btnAgregarColor.addEventListener("click", () => {
    const color = inputColor.value.trim();
    if (color && !colores.includes(color)) {
      colores.push(color);
      actualizarListaColores();
      inputColor.value = "";
      inputColor.focus();
    }
  });

  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const nombre = document.getElementById("nombre").value.trim();
    const descripcion = document.getElementById("descripcion").value.trim();
    const precio = parseFloat(document.getElementById("precio").value);

    if (!nombre || isNaN(precio)) {
      alert("Por favor completa todos los campos obligatorios.");
      return;
    }

    try {
      const productoPayload = { 
        nombre, 
        descripcion, 
        precio, 
        categoriaId: 1,
        colores: coloresSeleccionados
      };

      const backendBase = "http://localhost:8080/api/productos";

      const resProducto = await fetchConRefresh(backendBase, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(productoPayload),
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

      if (archivosSeleccionados.length > 0) {
        for (let i = 0; i < Math.min(archivosSeleccionados.length, 5); i++) {
          await subirArchivoBackend(productoCreado.id, archivosSeleccionados[i], i);
        }
      }

      alert("Producto, colores y archivos guardados con éxito!");
      form.reset();
      colores = [];
      actualizarListaColores();
      archivosSeleccionados = [];
      actualizarPreview();

    } catch (error) {
      alert("Error: " + error.message);
    }
  });
});
}