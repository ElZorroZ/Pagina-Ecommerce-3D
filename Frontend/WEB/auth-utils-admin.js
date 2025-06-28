function parseJwt(token) {
  if (!token || token.split('.').length < 3) {
    throw new Error("Token inválido");
  }

  const base64Url = token.split('.')[1];
  const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
  const jsonPayload = decodeURIComponent(
    atob(base64).split('').map(c =>
      '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
    ).join('')
  );
  return JSON.parse(jsonPayload);
}


const token = obtenerAccessToken();

if (token) {
  try {
    const payload = parseJwt(token);
    const roles = Array.isArray(payload.roles)
      ? payload.roles.map(r => (typeof r === "string" ? r : r.authority)).filter(Boolean)
      : [];

    if (!roles.includes("ROLE_ADMIN")) {
      // No sos admin
      window.location.href = "/WEB/index.html";
    }
  } catch (e) {
    console.error("Error al decodificar el token:", e);
    window.location.href = "/WEB/index.html"; // Si el token está mal, redirigimos igual
  }
} else {
  // No hay token, redirigir
  window.location.href = "/WEB/index.html";
}



function guardarUltimaPagina() {
  const currentPage = window.location.href;
  if (!currentPage.includes("login") && !currentPage.includes("register")) {
    localStorage.setItem("lastPage", currentPage);
  }
}

function obtenerAccessToken() {
  return localStorage.getItem("accessToken");
}
async function fetchConRefresh(url, options = {}) {
  options.headers = options.headers || {};
  if (!options.headers['Authorization']) {
    const token = localStorage.getItem('accessToken');
    if (token) {
      options.headers['Authorization'] = `Bearer ${token}`;
    }
  }

  let response = await fetch(url, options);

  if (response.status === 401) {
    // Intentar refrescar token
    const nuevoToken = await refreshAccessToken();
    if (nuevoToken) {
      options.headers['Authorization'] = `Bearer ${nuevoToken}`;
      response = await fetch(url, options);
    } else {
      // No se pudo refrescar, redirigir a login (refreshAccessToken ya lo hace)
      throw new Error('No autorizado');
    }
  }

  return response;
}

async function refreshAccessToken() {
  const refreshToken = localStorage.getItem("refreshToken");
  if (!refreshToken) {
    // No redirige automáticamente
    return null;
  }

  try {
    const response = await fetch("http://localhost:8080/api/auth/refresh", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ refreshToken }),
    });

    if (response.ok) {
      const data = await response.json();
      localStorage.setItem("accessToken", data.token);
      localStorage.setItem("refreshToken", data.refreshToken);
      return data.token;
    } else {
      return null;
    }
  } catch (err) {
    console.error("Error al refrescar el token", err);
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
