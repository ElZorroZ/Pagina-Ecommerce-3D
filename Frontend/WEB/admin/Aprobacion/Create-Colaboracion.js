document.addEventListener("DOMContentLoaded", () => {
  (() => {
    // Estado global
    window.colaboradorState = window.colaboradorState || {};

    const form = document.getElementById("form-producto");

    if (!form) return;

    form.addEventListener("submit", async (e) => {
      e.preventDefault();

      const gmail = document.getElementById("email").value.trim();

      if (!gmail) {
        mostrarError("Por favor completa el email.");
        return;
      }

      try {
        mostrarCarga("Agregando colaborador..."); // Mostrar overlay
        const colaboradorPayload = { gmail };

        const res = await authManager.fetchWithAuth(
          `${API_BASE_URL}/api/usuario/colaboradores`,
          {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(colaboradorPayload),
          }
        );

        if (!res.ok) {
          const text = await res.text();
          console.error("❌ Error al alternar permiso, status:", res.status, "body:", text);
          let errorMessage = "Error al alternar permiso.";
          try {
            const errData = JSON.parse(text);
            if (errData.message) errorMessage = errData.message;
          } catch {}
          throw new Error(errorMessage);
        }



        mostrarExito("Permiso alternado correctamente.");
        form.reset();
        await cargarColaboradores(); // actualiza la lista
      } catch (error) {
        mostrarError("Error: " + error.message);
      }finally {
        ocultarCarga(); // Ocultar overlay siempre
      }
    });
  })();
});
