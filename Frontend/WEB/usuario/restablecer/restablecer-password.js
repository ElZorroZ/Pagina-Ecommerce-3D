const API_BASE_URL = "https://forma-programada.onrender.com";

document.getElementById('reset-password-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  const messageDiv = document.getElementById('message');
  messageDiv.textContent = '';
  messageDiv.className = '';

  const email = e.target.email.value.trim();
  if (!email) {
    messageDiv.textContent = 'Por favor, ingresa un correo electrónico válido.';
    messageDiv.classList.add('error');
    return;
  }

  try {
    const res = await fetch(`${API_BASE_URL}/api/auth/reset-password-request`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ email })
    });

    if (res.ok) {
      messageDiv.textContent = 'Se envió un correo para restablecer la contraseña.';
      messageDiv.classList.add('success');
      e.target.reset();
    } else {
      const errText = await res.text();
      messageDiv.textContent = 'Error: ' + errText;
      messageDiv.classList.add('error');
    }
  } catch (error) {
    messageDiv.textContent = 'Error de conexión con el servidor.';
    messageDiv.classList.add('error');
    console.error(error);
  }
});
