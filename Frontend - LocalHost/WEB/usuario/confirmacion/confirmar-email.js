const API_BASE_URL = "http://localhost:8080";

window.addEventListener('DOMContentLoaded', async () => {
  const mensaje = document.getElementById('mensaje');
  const urlParams = new URLSearchParams(window.location.search);
  const token = urlParams.get('token');

  if (!token) {
    mensaje.textContent = "Token no proporcionado.";
    mensaje.classList.add("error");
    return;
  }

  try {
    // Cambié la URL para apuntar a tu endpoint de confirmación de email
    const res = await fetch(`${API_BASE_URL}/api/usuario/confirmar-email?token=${encodeURIComponent(token)}`);

    if (res.ok) {
      mensaje.textContent = "¡Email cambiado correctamente! Ya podés iniciar sesión.";
      mensaje.classList.add("success");
      // Redirigir al login luego de 3 segundos
      setTimeout(() => {
        window.location.href = '/usuario/login/login.html';
      }, 3000);
    } else {
      const error = await res.text();
      mensaje.textContent = "Error: " + error;
      mensaje.classList.add("error");
    }
  } catch (e) {
    mensaje.textContent = "Error de conexión con el servidor.";
    mensaje.classList.add("error");
    console.error(e);
  }
});
