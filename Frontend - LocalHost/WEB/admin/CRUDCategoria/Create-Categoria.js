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
  window.categoriaState = window.categoriaState || {};
  window.categoriaState.archivosSeleccionados = window.categoriaState.archivosSeleccionados || [];

  inputImagenes.addEventListener('change', () => {
    const files = Array.from(inputImagenes.files);

    // Si ya hay una imagen o se intenta cargar más de una
    if (
      window.categoriaState.archivosSeleccionados.length >= 1 ||
      files.length > 1
    ) {
      alert("Solo se permite subir una imagen. Primero eliminá la actual para agregar otra.");
      inputImagenes.value = ""; // Limpio el input
      return;
    }

    files.forEach(file => {
      const existe = window.categoriaState.archivosSeleccionados.some(
        f => f.name === file.name && f.size === file.size
      );
      if (!existe) window.categoriaState.archivosSeleccionados.push(file);
    });

    actualizarPreview();
    inputImagenes.value = "";
  });

  const form = document.getElementById("form-producto");
  const btnAgregarColor = document.getElementById("btn-agregar-color");

  function actualizarPreview() {
    preview.innerHTML = "";
    if (!window.categoriaState.archivosSeleccionados || window.categoriaState.archivosSeleccionados.length === 0) return;

    window.categoriaState.archivosSeleccionados.forEach((archivo, idx) => {
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
        window.categoriaState.archivosSeleccionados.splice(idx, 1);
        actualizarPreview();
      });

      div.appendChild(img);
      div.appendChild(btnEliminar);
      preview.appendChild(div);
    });
  }

  form.addEventListener("submit", async (e) => {
  e.preventDefault();

  const nombre = document.getElementById("nombre").value.trim();
  const descripcion = document.getElementById("descripcion").value.trim();

  if (!nombre || !descripcion) {
    alert("Por favor completa todos los campos obligatorios.");
    return;
  }

  const tieneArchivos = window.categoriaState.archivosSeleccionados.length > 0;
  const backendBaseSinImagen = `${API_BASE_URL}/api/categoria`; // Cambié el path a /categorias
  const backendBaseConImagen = `${API_BASE_URL}/api/categoria/crearCategoriaConImagen`;

  try {
    if (!tieneArchivos) {
      // Sin imagen: enviamos JSON normal al primer endpoint
      const categoriaPayload = { nombre, descripcion };

      const res = await fetchConRefresh(backendBaseSinImagen, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(categoriaPayload),
      });

      if (!res.ok) {
        const errorText = await res.text();
        throw new Error(errorText || "Error al guardar la categoría");
      }

      alert("Categoría guardada con éxito!");
      form.reset();
      window.categoriaState.archivosSeleccionados = [];
      actualizarPreview();
      await cargarCategorias();
      // Aquí podés actualizar la lista de categorías si querés
    } else {
      // Con imagen: enviamos multipart/form-data al segundo endpoint
      const formData = new FormData();

      // El backend espera la categoría como JSON en la parte "categoria"
      // Entonces transformamos el objeto a JSON string
      const categoriaObj = { nombre, descripcion };
      formData.append("categoria", new Blob([JSON.stringify(categoriaObj)], { type: "application/json" }));

      // Solo envío la primer imagen (o la que tengas)
      formData.append("file", window.categoriaState.archivosSeleccionados[0]);

      const res = await fetchConRefresh(backendBaseConImagen, {
        method: "POST",
        body: formData,
      });

      if (!res.ok) {
        const errorText = await res.text();
        throw new Error(errorText || "Error al guardar la categoría con imagen");
      }

      alert("Categoría con imagen guardada con éxito!");
      form.reset();
      window.categoriaState.archivosSeleccionados = [];
      actualizarPreview();
      cargarCategorias();
    }
  } catch (error) {
    alert("Error: " + error.message);
  }
});
})();

});
