// Login normal
document.getElementById("login-form").addEventListener("submit", async function (e) {
  e.preventDefault();

  const email = document.getElementById("email").value;
  const password = document.getElementById("password").value;

  if (!email || !password) {
    mostrarError("Por favor, completa todos los campos");
    return;
  }

  try {
    mostrarCarga("Iniciando sesión..."); // Overlay mientras se procesa
    const result = await authManager.login(email, password);

    if (result.success) {
      mostrarExito("¡Login exitoso! Redirigiendo...");

      const lastPage = localStorage.getItem("lastPage");
      setTimeout(() => {
        if (lastPage) {
          localStorage.removeItem("lastPage");
          window.location.href = lastPage;
        } else {
          window.location.href = "/index.html";
        }
      }, 1000);
    } else {
      mostrarError("Error al iniciar sesión: " + result.error);
      console.error("Error de login:", result.error);
    }
  } catch (error) {
    mostrarError("Ocurrió un error inesperado al intentar iniciar sesión. Intenta nuevamente.");
    console.error("Error inesperado en login:", error);
  } finally {
    ocultarCarga(); // Ocultar overlay siempre
  }
});

// Login con Google
document.getElementById("google-login").addEventListener("click", function () {
  // Redirige al flujo OAuth2 de Spring Security
  window.location.href = "http://localhost:8080/oauth2/authorization/google";
});

// Verificar si el usuario ya está autenticado al cargar la página
document.addEventListener('DOMContentLoaded', function() {
  if (authManager.isAuthenticated()) {
    console.log('Usuario ya autenticado, redirigiendo...');
    const lastPage = localStorage.getItem("lastPage");
    if (lastPage) {
      localStorage.removeItem("lastPage");
      window.location.href = lastPage;
    } else {
      window.location.href = "/index.html";
    }
  }
});
