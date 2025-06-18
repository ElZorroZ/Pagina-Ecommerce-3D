document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("register-form");

    form.addEventListener("submit", async (e) => {
      e.preventDefault();

      const nombre = document.getElementById("nombre").value.trim();
      const gmail = document.getElementById("email").value.trim();
      const password = document.getElementById("password").value;
      const confirmPassword = document.getElementById("confirm-password").value;

      if (password !== confirmPassword) {
        alert("Las contraseñas no coinciden.");
        return;
      }

      try {
        const response = await fetch("http://localhost:8080/api/auth/register", {
          method: "POST",
          headers: {
            "Content-Type": "application/json"
          },
          body: JSON.stringify({ nombre, gmail, password })
        });

        const data = await response.text();

        if (!response.ok) {
          alert("Error: " + data);
        } else {
          alert("Registro exitoso: " + data);
          form.reset();
        }
      } catch (error) {
        console.error("Error al registrar:", error);
        alert("Ocurrió un error al registrar el usuario.");
      }
    });
  });