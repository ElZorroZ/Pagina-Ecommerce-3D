const categoriesDropdown = document.getElementById('categories-dropdown');

// Load categories from API
async function loadCategories() {
    const categories = await API.getCategories();
    renderCategories(categories);
}

// Render categories in dropdown
function renderCategories(categories) {
    if (!Array.isArray(categories)) {
        console.error('Categorías inválidas:', categories);
        return;
    }

    categoriesDropdown.innerHTML = '';
    
    categories.forEach(category => {
        if (category.id === 1) return; // 👈 Saltar la categoría con id 1
        const categoryLink = document.createElement('a');
        categoryLink.href = '#';
        categoryLink.className = 'dropdown-category';
        categoryLink.textContent = category.nombre; // "nombre" según tu DTO
        categoryLink.dataset.categoryId = category.id;
        
        categoriesDropdown.appendChild(categoryLink);
    });
}
// Handle all click events
function handleClicks(e) {
    // Handle category clicks in dropdown
    if (e.target.classList.contains('dropdown-category')) {
        e.preventDefault();
        const categoryId = e.target.dataset.categoryId;
        const categoryName = e.target.textContent.toLowerCase().replace(/ /g, '-');
        if (categoryId) {
            window.location.href = `/categoria.html?categoria=${encodeURIComponent(categoryName)}`;
        }
    }

}


// Initialize shop dropdown functionality
function initializeDropdown() {
    const shopTrigger = document.getElementById('shop-trigger');
    const categoriesDropdownMenu = document.getElementById('categories-dropdown');

    if (shopTrigger && categoriesDropdownMenu) {
        // Show dropdown on hover
        shopTrigger.addEventListener('mouseenter', () => {
            categoriesDropdownMenu.classList.add('show');
        });
        
        // Hide dropdown when leaving the entire dropdown area
        const navDropdown = shopTrigger.parentElement;
        navDropdown.addEventListener('mouseleave', () => {
            categoriesDropdownMenu.classList.remove('show');
        });
    }
}

// --- Manejo de tokens ---
function obtenerAccessToken() {
  return localStorage.getItem('accessToken');
}

function obtenerRefreshToken() {
  return localStorage.getItem('refreshToken');
}

function guardarTokens(accessToken, refreshToken) {
  localStorage.setItem('accessToken', accessToken);
  localStorage.setItem('refreshToken', refreshToken);
}

function eliminarTokens() {
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
}

async function refreshAccessToken() {
  const refreshToken = obtenerRefreshToken();
  if (!refreshToken) return null;

  try {
    const res = await fetch('https://forma-programada.onrender.com/api/auth/refresh-token', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken })
    });

    if (!res.ok) {
      eliminarTokens();
      return null;
    }

    const data = await res.json();
    // Suponemos backend responde con { accessToken, refreshToken }
    guardarTokens(data.accessToken, data.refreshToken);
    return data.accessToken;

  } catch (e) {
    eliminarTokens();
    return null;
  }
}

function redirigirALogin() {
  eliminarTokens();
  window.location.href = '/WEB/usuario/login/login.html'; // Cambiar según ruta real
}

// --- Función fetch con refresh automático ---
async function fetchConRefresh(url, options = {}) {
  options.headers = options.headers || {};
  let token = obtenerAccessToken();

  if (token) {
    options.headers["Authorization"] = `Bearer ${token}`;
  }

  let response = await fetch(url, options);

  if (response.status === 401) {
    // Intentar refrescar token
    const nuevoToken = await refreshAccessToken();

    if (nuevoToken) {
      options.headers["Authorization"] = `Bearer ${nuevoToken}`;
      response = await fetch(url, options);
    } else {
      redirigirALogin();
      throw new Error("No autorizado");
    }
  }

  if (!response.ok) {
    const text = await response.text();
    throw new Error(text || "Error en la petición");
  }

  return response;
}

// --- Función para parsear JWT ---
function parseJwt(token) {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    return JSON.parse(jsonPayload);
  } catch {
    return null;
  }
}

// --- Modal cambio de contraseña ---
const btnEditarPassword = document.getElementById('btnEditarPassword');
const modalPassword = document.getElementById('modalCambiarPassword');
const cerrarModalPassword = document.getElementById('cerrarModalPassword');
const formCambiarPassword = document.getElementById('formCambiarPassword');

btnEditarPassword.addEventListener('click', () => {
  modalPassword.classList.add('active');
});

cerrarModalPassword.addEventListener('click', () => {
  modalPassword.classList.remove('active');
  formCambiarPassword.reset();
});

formCambiarPassword.addEventListener('submit', async (e) => {
  e.preventDefault();

  const passwordActual = formCambiarPassword.passwordActual.value.trim();
  const nuevaPassword = formCambiarPassword.nuevaPassword.value.trim();
  const repetirNuevaPassword = formCambiarPassword.repetirNuevaPassword.value.trim();

  if (nuevaPassword !== repetirNuevaPassword) {
    alert("Las nuevas contraseñas no coinciden.");
    return;
  }

  if (nuevaPassword.length < 6) {
    alert("La nueva contraseña debe tener al menos 6 caracteres.");
    return;
  }

  try {
    const token = obtenerAccessToken();
    if (!token) throw new Error("No hay token de autenticación");

    const payload = parseJwt(token);
    const gmail = payload?.sub;
    if (!gmail) throw new Error("Token inválido o sin gmail");

    const body = {
      passwordActual,
      nuevaPassword,
    };

    const res = await fetchConRefresh(`https://forma-programada.onrender.com/api/usuario/${encodeURIComponent(gmail)}/password-directo`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
    });

    alert("Contraseña actualizada correctamente.");
    modalPassword.classList.remove("active");
    formCambiarPassword.reset();

  } catch (error) {
    alert("Error: " + error.message);
  }
});

// --- Modal cambio de email ---
const btnCambiarEmail = document.getElementById('btnCambiarEmail');
const modalEmail = document.getElementById('modalCambiarEmail');
const cerrarModalEmail = document.getElementById('cerrarModalEmail');
const btnEnviarCambioEmail = document.getElementById('btnEnviarCambioEmail');
const inputNuevoEmail = document.getElementById('nuevoEmail');

btnCambiarEmail.addEventListener('click', () => {
  modalEmail.classList.add('active');
});

cerrarModalEmail.addEventListener('click', () => {
  modalEmail.classList.remove('active');
  inputNuevoEmail.value = '';
});

btnEnviarCambioEmail.addEventListener('click', async () => {
  const nuevoEmail = inputNuevoEmail.value.trim();
  if (!nuevoEmail) {
    alert("Por favor, ingresa un nuevo email.");
    return;
  }

  const token = obtenerAccessToken();
  if (!token) {
    alert("No estás autenticado");
    redirigirALogin();
    return;
  }

  const payload = parseJwt(token);
  const gmail = payload?.sub;
  if (!gmail) {
    alert("Token inválido");
    redirigirALogin();
    return;
  }

  try {
    const res = await fetchConRefresh(`https://forma-programada.onrender.com/api/usuario/${encodeURIComponent(gmail)}/email`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ nuevoEmail }),
    });

    alert("Se envió un email para confirmar el cambio de email.");
    modalEmail.classList.remove('active');
    inputNuevoEmail.value = '';

  } catch (error) {
    console.error("Error al cambiar email:", error);
    alert("Error: " + error.message);
  }
});

// --- Carga y actualización del perfil ---
document.addEventListener("DOMContentLoaded", async () => {
  initializeDropdown();
  loadCategories();
  document.addEventListener('click', handleClicks);

  
  const token = obtenerAccessToken();

  if (!token) {
    alert("No estás autenticado");
    redirigirALogin();
    return;
  }

  const payload = parseJwt(token);
  const gmail = payload?.sub;

  if (!gmail) {
    alert("Token inválido");
    redirigirALogin();
    return;
  }

  const form = document.getElementById("perfil-form");

  // Cargar datos del usuario
  try {
    const res = await fetchConRefresh(`https://forma-programada.onrender.com/api/usuario/${gmail}`);
    const usuario = await res.json();

    form.nombre.value = usuario.nombre ?? "";
    form.apellido.value = usuario.apellido ?? "";
    form.direccion.value = usuario.direccion ?? "";
    form.codigoPostal.value = usuario.cp ?? "";
    form.ciudad.value = usuario.ciudad ?? "";
    form.telefono.value = usuario.telefono ?? "";

    document.getElementById("email").value = usuario.gmail ?? "";

  } catch (err) {
    console.error(err);
    alert("Error al cargar el perfil: " + err.message);
  }

  // Manejar actualización del perfil
  form.addEventListener("submit", async e => {
    e.preventDefault();

    const datos = {
      nombre: form.nombre.value.trim(),
      apellido: form.apellido.value.trim(),
      direccion: form.direccion.value.trim(),
      cp: form.codigoPostal.value.trim(),
      ciudad: form.ciudad.value.trim(),
      telefono: form.telefono.value.trim()
    };

    try {
      const res = await fetchConRefresh(`https://forma-programada.onrender.com/api/usuario/${gmail}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(datos)
      });

      await res.json();
      alert("Perfil actualizado correctamente");
    } catch (err) {
      console.error(err);
      alert("Error al actualizar perfil: " + err.message);
    }
  });
});
