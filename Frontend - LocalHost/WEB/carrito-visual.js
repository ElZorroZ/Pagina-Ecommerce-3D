const cartCount = document.querySelector('.cart-count');
<<<<<<< HEAD
async function actualizarCantidadCarrito() {
=======

function actualizarCantidadCarrito() {
  const usuarioId = localStorage.getItem('usuarioId');
  const token = localStorage.getItem('accessToken');
>>>>>>> parent of 391f6a9 (Merge branch 'main' of https://github.com/ElZorroZ/Pagina-Ecommerce-3D)
  const cartCount = document.querySelector('#cart-count'); // Ajusta el selector a tu HTML

  // Verificar autenticación usando el authManager
  if (!authManager.isAuthenticated()) {
    console.warn('Usuario no autenticado');
    if (cartCount) {
      cartCount.textContent = '0';
    }
    return;
  }

<<<<<<< HEAD
  const usuarioId = authManager.getUserId();

  try {
    const response = await authManager.fetchWithAuth(`${API_BASE_URL}/api/carrito/verCarrito/${usuarioId}`);
    
    if (!response.ok) {
      throw new Error(`Error en la respuesta del servidor: ${response.status}`);
=======
  fetch(`http://localhost:8080/api/carrito/verCarrito/${usuarioId}`, {
    headers: {
      'Authorization': `Bearer ${token}`
>>>>>>> parent of 391f6a9 (Merge branch 'main' of https://github.com/ElZorroZ/Pagina-Ecommerce-3D)
    }

    const data = await response.json();
    
    // data ahora es un array de IDs, así que la cantidad es su longitud
    const cantidad = data.length;
    if (cartCount) {
      cartCount.textContent = cantidad;
    }
    
    console.log(`Carrito actualizado: ${cantidad} items`);
    
  } catch (error) {
    console.error('Error al cargar carrito:', error);
    
    // En caso de error, mostrar 0 en el contador
    if (cartCount) {
      cartCount.textContent = '0';
    }
    
    // Si el error es de autenticación, el authManager ya manejará la redirección
    if (error.message.includes('Usuario no autenticado') || error.message.includes('Sesión expirada')) {
      console.warn('Sesión expirada, redirigiendo al login...');
    }
  }
}
document.addEventListener('DOMContentLoaded', () => {
  actualizarCantidadCarrito();
});
