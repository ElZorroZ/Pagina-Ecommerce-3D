document.getElementById("login-form").addEventListener("submit", async function (e) {
  e.preventDefault();

  const email = document.getElementById("email").value;
  const password = document.getElementById("password").value;

  // Validación básica del frontend
  if (!email || !password) {
    alert("Por favor, completa todos los campos");
    return;
  }

  try {
    // Usar el authManager para el login
    const result = await authManager.login(email, password);

    if (result.success) {
      console.log("Login exitoso:", result.data);
      
      // Mostrar mensaje de éxito (opcional)
      // alert("¡Login exitoso! Bienvenido/a");

      // Redirigir a la última página visitada o al inicio
      const lastPage = localStorage.getItem("lastPage");
      if (lastPage) {
        localStorage.removeItem("lastPage"); // Limpiar después de usar
        window.location.href = lastPage;
      } else {
        window.location.href = "/index.html";
      }
    } else {
      // Mostrar error específico del servidor
      alert("Error al iniciar sesión: " + result.error);
      console.error("Error de login:", result.error);
    }

  } catch (error) {
    console.error("Error inesperado en login:", error);
    alert("Ocurrió un error inesperado al intentar iniciar sesión. Por favor, intenta nuevamente.");
  }
});

// Opcional: Verificar si el usuario ya está autenticado al cargar la página
document.addEventListener('DOMContentLoaded', function() {
  if (authManager.isAuthenticated()) {
    console.log('Usuario ya autenticado, redirigiendo...');
    
    // Redirigir a la última página o al inicio
    const lastPage = localStorage.getItem("lastPage");
    if (lastPage) {
      localStorage.removeItem("lastPage");
      window.location.href = lastPage;
    } else {
      window.location.href = "/index.html";
    }
  }
});

// Opcional: Manejar el botón "Recordarme" si lo tienes
// const rememberCheckbox = document.getElementById("remember-me");
// if (rememberCheckbox) {
//   rememberCheckbox.addEventListener('change', function() {
//     // Implementar lógica de "recordarme" si es necesario
//     console.log('Recordarme:', this.checked);
//   });
// }
