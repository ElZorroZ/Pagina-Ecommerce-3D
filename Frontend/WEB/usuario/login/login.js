const API_BASE_URL = "https://forma-programada.onrender.com";
document.getElementById("login-form").addEventListener("submit", async function (e) {
  e.preventDefault();

  const email = document.getElementById("email").value;
  const password = document.getElementById("password").value;

  try {
    const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        gmail: email,
        password: password
      })
    });

    if (!response.ok) {
      const errorText = await response.text();
      alert("Error al iniciar sesión: " + errorText);
      return;
    }

    const data = await response.json();
  console.log("Respuesta del login:", data);

    localStorage.setItem("accessToken", data.accessToken);
    localStorage.setItem("refreshToken", data.refreshToken);

    if (data.usuarioId !== undefined) {
      localStorage.setItem("usuarioId", data.usuarioId.toString());

      alert("Usuario ID guardado: " + data.usuarioId);
    } else {
      alert("No se recibió usuarioId en la respuesta");
    }


    const lastPage = localStorage.getItem("lastPage");
    if (lastPage) {
      window.location.href = lastPage;
    } else {
      window.location.href = "/index.html";
    }
  } catch (error) {
    console.error("Error en la solicitud:", error);
    alert("Ocurrió un error al intentar iniciar sesión.");
  }
});
