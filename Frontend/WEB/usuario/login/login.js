document.getElementById("login-form").addEventListener("submit", async function (e) {
  e.preventDefault();

  const email = document.getElementById("email").value;
  const password = document.getElementById("password").value;

  try {
    const response = await fetch("http://localhost:8080/api/auth/login", {
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
    console.log(data)
    localStorage.setItem("accessToken", data.accessToken);
    localStorage.setItem("refreshToken", data.refreshToken);

    const lastPage = localStorage.getItem("lastPage");
    if (lastPage) {
      window.location.href = lastPage;
    } else {
      window.location.href = "/WEB/index.html";
    }
    alert("Inicio")
  } catch (error) {
    console.error("Error en la solicitud:", error);
    alert("Ocurrió un error al intentar iniciar sesión.");
  }
});
