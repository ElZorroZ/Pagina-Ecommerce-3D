document.addEventListener("DOMContentLoaded", () => {
  (async () => {

<<<<<<< HEAD
    window.categoriaState = window.categoriaState || {};
    const form = document.getElementById("form-producto");
    if (!form) return;
=======
  try {
    const response = await fetch("http://localhost:8080/api/auth/refresh", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ refreshToken }),
    });
>>>>>>> parent of 391f6a9 (Merge branch 'main' of https://github.com/ElZorroZ/Pagina-Ecommerce-3D)

    form.addEventListener("submit", async (e) => {
      e.preventDefault();

      const nombre = document.getElementById("nombre").value.trim();
      if (!nombre) {
        mostrarError("Por favor completa todos los campos obligatorios.");
        return;
      }

      const backendBaseSinImagen = `${API_BASE_URL}/api/categoria`;
      const categoriaPayload = { nombre };

      try {
        const res = await authManager.fetchWithAuth(backendBaseSinImagen, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(categoriaPayload)
        });

        if (!res.ok) {
          const errorText = await res.text();
          throw new Error(errorText || "Error al guardar la categoría");
        }

        mostrarExito("Categoría guardada con éxito!");
        form.reset();
        await cargarCategorias();
      } catch (error) {
        mostrarError("Error: " + error.message);
        console.error(error);
      }
    });

<<<<<<< HEAD
  })();
=======
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
  const backendBaseSinImagen = "http://localhost:8080/api/categoria"; // Cambié el path a /categorias
  const backendBaseConImagen = "http://localhost:8080/api/categoria/crearCategoriaConImagen";

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

>>>>>>> parent of 391f6a9 (Merge branch 'main' of https://github.com/ElZorroZ/Pagina-Ecommerce-3D)
});
