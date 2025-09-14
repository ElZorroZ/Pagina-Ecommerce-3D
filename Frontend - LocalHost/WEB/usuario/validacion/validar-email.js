window.addEventListener('DOMContentLoaded', async () => {
  const urlParams = new URLSearchParams(window.location.search);
  const token = urlParams.get('token');

  if (!token) {
    mostrarError("Token no proporcionado.");
    return;
  }

  try {
    mostrarCarga("Validando cuenta...");

    const res = await fetch(`${API_BASE_URL}/api/auth/validate?token=${encodeURIComponent(token)}`);

    if (res.ok) {
      mostrarExito("¡Cuenta validada correctamente! Ya podés iniciar sesión.");
      // Redirigir al login luego de 3 segundos
      setTimeout(() => {
        window.location.href = '/usuario/login/login.html';
      }, 3000);
    } else {
      const errorText = await res.text();
      mostrarError("Error: " + errorText);
      console.error("Error en validar cuenta:", errorText);
    }

  } catch (e) {
    mostrarError("Error de conexión con el servidor.");
    console.error("Error inesperado al validar cuenta:", e);
  } finally {
    ocultarCarga();
  }
});
