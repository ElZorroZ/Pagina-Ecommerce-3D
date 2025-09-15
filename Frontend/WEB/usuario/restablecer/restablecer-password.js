document.getElementById('reset-password-form').addEventListener('submit', async (e) => {
  e.preventDefault();

  const email = e.target.email.value.trim();
  if (!email) {
    mostrarError('Por favor, ingresa un correo electrónico válido.');
    return;
  }

  try {
    mostrarCarga("Enviando solicitud de restablecimiento...");
    
    const res = await fetch(`${API_BASE_URL}/api/auth/reset-password-request`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email })
    });

    if (res.ok) {
      mostrarExito('Se envió un correo para restablecer la contraseña.');
      e.target.reset();
    } else {
      const errText = await res.text();
      mostrarError('Error: ' + errText);
      console.error('Error en reset-password-request:', errText);
    }

  } catch (error) {
    mostrarError('Error de conexión con el servidor.');
    console.error('Error inesperado en reset-password-request:', error);
  } finally {
    ocultarCarga();
  }
});
