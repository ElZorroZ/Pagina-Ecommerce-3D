const cartCount = document.querySelector('.cart-count');

function actualizarCantidadCarrito() {
  const usuarioId = localStorage.getItem('usuarioId');
  const token = localStorage.getItem('accessToken');

  if (!usuarioId || !token) {
    console.warn('No hay usuarioId o token en localStorage');
    return;
  }

  fetch(`http://localhost:8080/api/carrito/verCarrito/${usuarioId}`, {
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
      // Asumiendo que data es un array con los productos en carrito
      const cantidad = data.reduce((total, item) => total + (item.cantidad || 1), 0);
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
