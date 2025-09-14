document.getElementById('register-form').onsubmit = async e => {
  e.preventDefault();

  const nombre = document.getElementById('nombre').value.trim();
  const apellido = document.getElementById('apellido').value.trim();
  const gmail = document.getElementById('email').value.trim();
  const password = document.getElementById('password').value;
  const confirm = document.getElementById('confirm-password').value;

  if (password !== confirm) {
    mostrarError('Las contraseñas no coinciden');
    return;
  }

  try {
    mostrarCarga("Registrando usuario...");

    // Usamos directamente fetch con la URL completa
    const res = await fetch(`${API_BASE_URL}/api/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ nombre, apellido, gmail, password })
    });

    if (res.ok) {
      mostrarExito('Registrado correctamente. Revisa tu email para confirmar tu cuenta.');
    } else {
      const errorText = await res.text();
      mostrarError('Error al registrar: ' + errorText);
      console.error('Error en registro:', errorText);
    }

  } catch (err) {
    mostrarError('Ocurrió un error de conexión al registrar.');
    console.error('Error inesperado en registro:', err);
  } finally {
    ocultarCarga();
  }
};
