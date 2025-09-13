const cartCount = document.querySelector('.cart-count');

function actualizarCantidadCarrito() {
  const usuarioId = localStorage.getItem('usuarioId');
  const token = localStorage.getItem('accessToken');
  const cartCount = document.querySelector('#cart-count'); // Ajusta el selector a tu HTML

  if (!usuarioId || !token) {
    console.warn('No hay usuarioId o token en localStorage');
    return;
  }

  fetch(`https://forma-programada.onrender.com/api/carrito/verCarrito/${usuarioId}`, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
    .then(response => {
      if (!response.ok) {
        throw new Error('Error en la respuesta del servidor');
      }
      return response.json();
    })
    .then(data => {
      // data ahora es un array de IDs, así que la cantidad es su longitud
      const cantidad = data.length;
      if (cartCount) {
        cartCount.textContent = cantidad;
      }
    })
    .catch(error => {
      console.error('Error al cargar carrito:', error);
    });
}


document.addEventListener('DOMContentLoaded', () => {
  actualizarCantidadCarrito();
});
