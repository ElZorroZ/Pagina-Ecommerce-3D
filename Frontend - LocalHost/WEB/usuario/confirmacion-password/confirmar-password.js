window.addEventListener('DOMContentLoaded', () => {
  const formulario = document.getElementById('formularioCambioPassword');
  const urlParams = new URLSearchParams(window.location.search);
  const token = urlParams.get('token');

  if (!token) {
    mostrarError("Token no proporcionado.");
    return;
  }

  // Mostrar el formulario
  mostrarExito("Ingresá tu nueva contraseña.");
  formulario.style.display = "block";

  formulario.addEventListener('submit', async (e) => {
    e.preventDefault();

    const pass1 = document.getElementById('password1').value.trim();
    const pass2 = document.getElementById('password2').value.trim();

    if (pass1.length < 6) {
      mostrarError("La contraseña debe tener al menos 6 caracteres.");
      return;
    }

    if (pass1 !== pass2) {
      mostrarError("Las contraseñas no coinciden.");
      return;
    }

    try {
      mostrarCarga("Actualizando contraseña...");

      const cambiarRes = await fetch(`${API_BASE_URL}/api/auth/reset-password/confirm`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          token: token,
          nuevaPassword: pass1
        })
      });

      ocultarCarga();

      if (cambiarRes.ok) {
        mostrarExito("Contraseña cambiada correctamente. Redirigiendo al login...");
        setTimeout(() => {
          window.location.href = '/usuario/login/login.html';
        }, 3000);
      } else {
        const errorText = await cambiarRes.text();
        mostrarError("Error: " + errorText);
      }
    } catch (error) {
      ocultarCarga();
      mostrarError("Error de conexión con el servidor.");
      console.error(error);
    }
  });
});
