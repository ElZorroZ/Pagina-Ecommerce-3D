function guardarUltimaPagina() {
  const currentPage = window.location.href;
  if (!currentPage.includes("login") && !currentPage.includes("register")) {
    localStorage.setItem("lastPage", currentPage);
  }
}

async function fetchConAuth(url, options = {}) {
  // Poner Authorization si no está
  options.headers = options.headers || {};
  if (!options.headers['Authorization']) {
    const token = obtenerAccessToken();
    if (token) {
      options.headers['Authorization'] = 'Bearer ' + token;
    }
  }

  let response = await fetch(url, options);

  if (response.status === 401) {
    // Token expirado o inválido, intentamos refrescar
    const nuevoToken = await refreshAccessToken();
    if (nuevoToken) {
      // Actualizamos header con el nuevo token y reintentamos
      options.headers['Authorization'] = 'Bearer ' + nuevoToken;
      response = await fetch(url, options);
    } else {
      // No se pudo refrescar, redirigir a login
      redirigirALogin();
      throw new Error("No autorizado, redirigiendo a login.");
    }
  }

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(errorText || 'Error en la petición');
  }

  return response;
}

function obtenerAccessToken() {
  return localStorage.getItem("accessToken");
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
      localStorage.setItem("accessToken", data.accessToken);
      localStorage.setItem("refreshToken", data.refreshToken);
      return data.accessToken;
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

function base64UrlDecode(str) {
  str = str.replace(/-/g, '+').replace(/_/g, '/');
  while (str.length % 4) {
    str += '=';
  }
  return atob(str);
}


function obtenerRolesDesdeToken() {
  const token = localStorage.getItem("accessToken");
  if (!token) return [];

  try {
    const payloadBase64 = token.split(".")[1];
    const payloadJson = base64UrlDecode(payloadBase64);
    const payload = JSON.parse(payloadJson);
    const roles = payload.roles || [];

    return roles.map(r => r.authority);
  } catch (e) {
    console.error("Error al decodificar el token:", e);
    return [];
  }
}
window.addEventListener("load", () => {
  // Obtener roles del token (asumiendo que la función obtenerRolesDesdeToken() está definida)
  const roles = obtenerRolesDesdeToken();

  // Obtener elementos del DOM
  const adminPanel = document.getElementById("adminPanel");
  const loginLink = document.getElementById("loginLink");

  // Mostrar/ocultar Panel de Administración según rol ADMIN
  if (adminPanel) {
    if (roles.includes("ADMIN")) {
      adminPanel.style.display = ""; // Mostrar
    } else {
      adminPanel.style.display = "none"; // Ocultar
    }
  }

  // Mostrar/ocultar LOGIN según si está logueado
  if (loginLink) {
    const token = obtenerAccessToken();
    if (token) {
      loginLink.style.display = "none"; // Ocultar login si está logueado
      cuentaLink.style.display = "";
    } else {
      loginLink.style.display = ""; // Mostrar login si no está logueado
    }
  }
});

console.log(localStorage.getItem("accessToken"));
// Llamar al cargar la página
window.addEventListener("load", () => {
  guardarUltimaPagina();
});

window.refreshAccessToken = refreshAccessToken;
window.redirigirALogin = redirigirALogin;
window.guardarUltimaPagina = guardarUltimaPagina;
window.obtenerAccessToken = obtenerAccessToken;
