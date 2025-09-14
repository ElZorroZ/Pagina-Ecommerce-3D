const categoriesDropdown = document.getElementById('categories-dropdown');

// --- Cargar categorías ---
async function loadCategories() {
  try {
    const categories = await API.getCategories();
    renderCategories(categories);
  } catch (err) {
    console.error("Error al cargar categorías:", err);
  }
}

function renderCategories(categories) {
  if (!Array.isArray(categories)) {
    console.error('Categorías inválidas:', categories);
    return;
  }

  categoriesDropdown.innerHTML = '';
  categories.forEach(category => {
    if (category.id === 1) return; // Saltar la categoría con id 1
    const categoryLink = document.createElement('a');
    categoryLink.href = '#';
    categoryLink.className = 'dropdown-category';
    categoryLink.textContent = category.nombre;
    categoryLink.dataset.categoryId = category.id;
    categoriesDropdown.appendChild(categoryLink);
  });
}

function handleClicks(e) {
  if (e.target.classList.contains('dropdown-category')) {
    e.preventDefault();
    const categoryId = e.target.dataset.categoryId;
    const categoryName = e.target.textContent.toLowerCase().replace(/ /g, '-');
    if (categoryId) {
      window.location.href = `/categoria.html?categoria=${encodeURIComponent(categoryName)}`;
    }
  }
}

function initializeDropdown() {
  const shopTrigger = document.getElementById('shop-trigger');
  const categoriesDropdownMenu = document.getElementById('categories-dropdown');

  if (shopTrigger && categoriesDropdownMenu) {
    shopTrigger.addEventListener('mouseenter', () => {
      categoriesDropdownMenu.classList.add('show');
    });

    const navDropdown = shopTrigger.parentElement;
    navDropdown.addEventListener('mouseleave', () => {
      categoriesDropdownMenu.classList.remove('show');
    });
  }
}
// --- Función helper mínima para 403 ---
async function fetchCon403(url, opciones = {}) {
  const res = await authManager.fetchWithAuth(url, opciones);

  if (res.status === 403) {
    alert("No autorizado. Redirigiendo al login...");
    window.location.href = '/usuario/login/login.html';
    throw new Error("No autorizado");
  }

  return res;
}
// --- Modal cambio de contraseña ---
const btnEditarPassword = document.getElementById('btnEditarPassword');
const modalPassword = document.getElementById('modalCambiarPassword');
const cerrarModalPassword = document.getElementById('cerrarModalPassword');
const formCambiarPassword = document.getElementById('formCambiarPassword');

// --- Modal cambio de email ---
const btnCambiarEmail = document.getElementById('btnCambiarEmail');
const modalEmail = document.getElementById('modalCambiarEmail');
const cerrarModalEmail = document.getElementById('cerrarModalEmail');
const btnEnviarCambioEmail = document.getElementById('btnEnviarCambioEmail');
const inputNuevoEmail = document.getElementById('nuevoEmail');

btnEditarPassword.addEventListener('click', () => {
  modalPassword.classList.add('active');
});

cerrarModalPassword.addEventListener('click', () => {
  modalPassword.classList.remove('active');
  formCambiarPassword.reset();
});

btnEnviarCambioEmail.addEventListener('click', async () => {
  const nuevoEmail = inputNuevoEmail.value.trim();
  if (!nuevoEmail) {
    mostrarError("Por favor, ingresa un nuevo email.");
    return;
  }

  try {
    const user = authManager.getUserInfo();
    if (!user?.gmail) throw new Error("No se pudo obtener el usuario autenticado");

    mostrarCarga("Enviando confirmación de email...");

    const res = await authManager.fetchWithAuth(
      `${API_BASE_URL}/api/usuario/${encodeURIComponent(user.gmail)}/email`,
      {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ nuevoEmail }),
      }
    );

    ocultarCarga();

    if (res.ok) {
      mostrarExito("Se envió un email para confirmar el cambio de email.");
      modalEmail.classList.remove('active');
      inputNuevoEmail.value = '';
    } else {
      const msg = await res.text();
      mostrarError(`Error al cambiar email: ${msg}`);
    }
  } catch (error) {
    ocultarCarga();
    console.error("Error al cambiar email:", error);
    mostrarError("Error: " + error.message);
  }
});



btnCambiarEmail.addEventListener('click', () => {
  modalEmail.classList.add('active');
});

cerrarModalEmail.addEventListener('click', () => {
  modalEmail.classList.remove('active');
  inputNuevoEmail.value = '';
});
// --- Cambiar contraseña ---
formCambiarPassword.addEventListener('submit', async (e) => {
  e.preventDefault();

  const passwordActual = document.getElementById('passwordActual').value.trim();
  const nuevaPassword = document.getElementById('nuevaPassword').value.trim();
  const repetirNuevaPassword = document.getElementById('repetirNuevaPassword').value.trim();

  if (!passwordActual || !nuevaPassword || !repetirNuevaPassword) {
    mostrarError("Todos los campos son obligatorios.");
    return;
  }

  if (nuevaPassword.length < 6) {
    mostrarError("La nueva contraseña debe tener al menos 6 caracteres.");
    return;
  }

  if (nuevaPassword !== repetirNuevaPassword) {
    mostrarError("Las nuevas contraseñas no coinciden.");
    return;
  }

  try {
    const user = authManager.getUserInfo();
    if (!user?.gmail) throw new Error("No se pudo obtener el usuario autenticado");

    mostrarCarga("Actualizando contraseña...");

    const res = await authManager.fetchWithAuth(
      `${API_BASE_URL}/api/usuario/${encodeURIComponent(user.gmail)}/password-directo`,
      {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          passwordActual,
          nuevaPassword
        }),
      }
    );

    ocultarCarga();

    if (res.ok) {
      mostrarExito("Contraseña actualizada correctamente.");
      modalPassword.classList.remove('active');
      formCambiarPassword.reset();
    } else {
      const msg = await res.text();
      mostrarError(`Error al cambiar contraseña: ${msg}`);
    }

  } catch (error) {
    ocultarCarga();
    console.error("Error al cambiar contraseña:", error);
    mostrarError("Error: " + error.message);
  }
});

btnEnviarCambioEmail.addEventListener('click', async () => {
  const nuevoEmail = inputNuevoEmail.value.trim();
  if (!nuevoEmail) {
    mostrarError("Por favor, ingresa un nuevo email.");
    return;
  }

  try {
    const user = authManager.getUserInfo();
    if (!user?.gmail) throw new Error("No se pudo obtener el usuario autenticado");

    mostrarCarga("Enviando confirmación de email...");

    const res = await authManager.fetchWithAuth(
      `${API_BASE_URL}/api/usuario/${encodeURIComponent(user.gmail)}/email`,
      {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ nuevoEmail }),
      }
    );

    ocultarCarga();

    if (res.ok) {
      mostrarExito("Se envió un email para confirmar el cambio de email.");
      modalEmail.classList.remove('active');
      inputNuevoEmail.value = '';
    } else {
      const msg = await res.text();
      mostrarError(`Error al cambiar email: ${msg}`);
    }
  } catch (error) {
    ocultarCarga();
    console.error("Error al cambiar email:", error);
    mostrarError("Error: " + error.message);
  }
});


// --- Perfil ---
document.addEventListener("DOMContentLoaded", async () => {
  initializeDropdown();
  loadCategories();
  document.addEventListener('click', handleClicks);

  if (!authManager.isAuthenticated()) {
    alert("No estás autenticado");
    authManager.redirigirALogin();
    return;
  }

  const user = authManager.getUserInfo();
  if (!user?.gmail) {
    alert("Token inválido");
    authManager.redirigirALogin();
    return;
  }

  const telInput = document.getElementById("telefono");
  telInput.addEventListener("input", () => {
    telInput.value = telInput.value.replace(/[^0-9]/g, "");
  });

  const form = document.getElementById("perfil-form");

  try {
    const res = await authManager.fetchWithAuth(`${API_BASE_URL}/api/usuario/${user.gmail}`);

      if (!res.ok) {
          throw new Error(`Error ${res.status}: ${res.statusText}`);
      }

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
    mostrarCarga("Actualizando perfil...");

    const res = await authManager.fetchWithAuth(`${API_BASE_URL}/api/usuario/${user.gmail}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(datos),
    });

    ocultarCarga();

    if (res.ok) {
      mostrarExito("Perfil actualizado correctamente");
    } else {
      const msg = await res.text();
      mostrarError(`Error al actualizar perfil: ${msg}`);
    }
  } catch (err) {
    ocultarCarga();
    console.error(err);
    mostrarError("Error al actualizar perfil: " + err.message);
  }
});
});
