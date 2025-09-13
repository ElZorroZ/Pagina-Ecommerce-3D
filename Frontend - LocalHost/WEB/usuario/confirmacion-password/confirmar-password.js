window.addEventListener('DOMContentLoaded', () => {
  const mensaje = document.getElementById('mensaje');
  const formulario = document.getElementById('formularioCambioPassword');
  const urlParams = new URLSearchParams(window.location.search);
  const token = urlParams.get('token');

  if (!token) {
    mensaje.textContent = "Token no proporcionado.";
    mensaje.classList.add("error");
    return;
  }

  // Mostrar el formulario sin validar el token
  mensaje.textContent = "Ingresá tu nueva contraseña.";
  mensaje.classList.remove("error");
  mensaje.classList.add("success");
  formulario.style.display = "block";

  formulario.addEventListener('submit', async (e) => {
    e.preventDefault();

    const pass1 = document.getElementById('password1').value;
    const pass2 = document.getElementById('password2').value;

    if (pass1.length < 6) {
      mensaje.textContent = "La contraseña debe tener al menos 6 caracteres.";
      mensaje.classList.add("error");
      return;
    }

    if (pass1 !== pass2) {
      mensaje.textContent = "Las contraseñas no coinciden.";
      mensaje.classList.add("error");
      return;
    }

    try {
      // Enviar el cambio de contraseña al backend (POST)
      const cambiarRes = await fetch('http://localhost:8080/api/auth/reset-password/confirm', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          token: token,
          nuevaPassword: pass1
        })
      });

      if (cambiarRes.ok) {
        mensaje.textContent = "Contraseña cambiada correctamente. Redirigiendo al login...";
        mensaje.classList.remove("error");
        mensaje.classList.add("success");

        // Opcional: esperar 3 seg y redirigir
        setTimeout(() => {
          window.location.href = '/usuario/login/login.html';
        }, 3000);
      } else {
        const errorText = await cambiarRes.text();
        mensaje.textContent = "Error: " + errorText;
        mensaje.classList.add("error");
      }
    } catch (error) {
      mensaje.textContent = "Error de conexión con el servidor.";
      mensaje.classList.add("error");
      console.error(error);
    }
  });
});
