document.addEventListener("DOMContentLoaded", () => {
  (async () => {

    window.categoriaState = window.categoriaState || {};
    const form = document.getElementById("form-producto");
    if (!form) return;

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

  })();
});
