function guardarUltimaPagina() {
  const currentPage = window.location.href;
  if (!currentPage.includes("login") && !currentPage.includes("register")) {
    localStorage.setItem("lastPage", currentPage);
  }
}

function obtenerAccessToken() {
  return localStorage.getItem("accessToken");
}
async function refreshAccessToken() {
  const refreshToken = localStorage.getItem("refreshToken");
  if (!refreshToken) {
    redirigirALogin();
    return null;
  }

  try {
    const response = await fetch("http://localhost:8080/api/auth/refresh", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ refreshToken: refreshToken }),
    });

    if (response.ok) {
      const data = await response.json();
      localStorage.setItem("accessToken", data.accessToken);
      // refreshToken también se renueva por si expira pronto
      localStorage.setItem("refreshToken", data.refreshToken);
      return data.accessToken;
    } else {
      redirigirALogin();
      return null;
    }
  } catch (err) {
    console.error("Error al refrescar el token", err);
    redirigirALogin();
    return null;
  }
}

function redirigirALogin() {
  localStorage.removeItem("accessToken");
  localStorage.removeItem("refreshToken");
  window.location.href = "/login.html";
}

window.refreshAccessToken = refreshAccessToken;
window.redirigirALogin = redirigirALogin;
window.guardarUltimaPagina = guardarUltimaPagina;
window.obtenerAccessToken = obtenerAccessToken;
