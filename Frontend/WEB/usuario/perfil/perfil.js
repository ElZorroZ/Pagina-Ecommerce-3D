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
      passwordActual: formCambiarPassword.passwordActual.value.trim(),
      nuevaPassword: formCambiarPassword.nuevaPassword.value.trim(),
    };

    const res = await fetch(`http://localhost:8080/api/usuario/${encodeURIComponent(gmail)}/password-directo`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}`,
      },
      body: JSON.stringify(body),
    });

    if (!res.ok) {
      const errorText = await res.text();
      throw new Error(errorText || "Error al cambiar la contraseña");
    }

    alert("Contraseña actualizada correctamente.");
    modalPassword.classList.remove("active");
    formCambiarPassword.reset();
  } catch (error) {
    alert("Error: " + error.message);
  }
});

// Abrir y cerrar modal Email
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

  // Obtené el gmail del usuario (por ejemplo del token JWT)
  const token = obtenerAccessToken(); // función que tengas para obtener el JWT
  if (!token) {
    alert("No estás autenticado");
    return;
  }

  const payload = parseJwt(token); // función para decodificar JWT (la que ya usás)
  const gmail = payload?.sub;
  if (!gmail) {
    alert("Token inválido");
    return;
  }

  try {
    const res = await fetch(`http://localhost:8080/api/usuario/${encodeURIComponent(gmail)}/email`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}`
      },
      body: JSON.stringify({ nuevoEmail })
    });

    if (!res.ok) {
      const errorText = await res.text();
      throw new Error(errorText || "Error al cambiar el email");
    }

    alert("Se envió un email para confirmar el cambio de email.");
    modalEmail.classList.remove('active');
    inputNuevoEmail.value = '';

  } catch (error) {
    console.error("Error al cambiar email:", error);
    alert("Error: " + error.message);
    }
});
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

// --- Función fetch con refresh automático ---
async function fetchConRefresh(url, options = {}) {
  options.headers = options.headers || {};
  let token = obtenerAccessToken();

  if (token) {
    options.headers["Authorization"] = `Bearer ${token}`;
  }

  let response = await fetch(url, options);

  if (response.status === 401) {
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

// --- Carga y actualización del perfil ---
document.addEventListener("DOMContentLoaded", async () => {
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
    const res = await fetchConRefresh(`http://localhost:8080/api/usuario/${gmail}`);
    const usuario = await res.json();

    form.nombre.value = usuario.nombre ?? "";
    form.apellido.value = usuario.apellido ?? "";
    form.direccion.value = usuario.direccion ?? "";
    form.codigoPostal.value = usuario.cp ?? "";
    form.ciudad.value = usuario.ciudad ?? "";
    form.telefono.value = usuario.telefono ?? "";

     //Sección Email
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
      const res = await fetchConRefresh(`http://localhost:8080/api/usuario/${gmail}`, {
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
