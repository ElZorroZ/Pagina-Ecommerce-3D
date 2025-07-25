const BACKEND_URL = 'http://localhost:8080/api/auth/register';

document.getElementById('register-form').onsubmit = async e => {
  e.preventDefault();

  const nombre = document.getElementById('nombre').value.trim();
  const apellido = document.getElementById('apellido').value.trim();
  const gmail = document.getElementById('email').value.trim();
  const password = document.getElementById('password').value;
  const confirm = document.getElementById('confirm-password').value;

  if (password !== confirm) {
    alert('Las contraseñas no coinciden');
    return;
  }

  const res = await fetch(BACKEND_URL, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ nombre,apellido, gmail, password })
  });

  if (res.ok) {
    alert('Registrado correctamente. Revisa tu email.');
  } else {
    alert('Error: ' + await res.text());
  }
};
