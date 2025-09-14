const cartBtn = document.querySelector('.cart-btn'); 
const cartCount = document.querySelector('.cart-count');

async function actualizarCantidadCarrito() {
  if (!authManager.isAuthenticated()) {
    console.warn('Usuario no autenticado');
    if (cartCount) cartCount.textContent = '0';
    return;
  }

  const usuarioId = authManager.getUserId();

  try {
    const response = await authManager.fetchWithAuth(`${API_BASE_URL}/api/carrito/verCarrito/${usuarioId}`);
    
    if (!response.ok) {
      throw new Error(`Error en la respuesta del servidor: ${response.status}`);
    }

    const data = await response.json();
    console.log("Datos recibidos del carrito:", data);

    // Sumar cantidades totales sin filtrar duplicados
    let cantidadTotal = 0;
    data.forEach(item => {
      cantidadTotal += Number(item.cantidad) || 0;
    });

    if (cartCount) cartCount.textContent = cantidadTotal;

    console.log(`Carrito actualizado: ${cantidadTotal} items (sumando todas las cantidades)`);

  } catch (error) {
    console.error('Error al cargar carrito:', error);
    if (cartCount) cartCount.textContent = '0';
  }
}

if (cartBtn) {
  cartBtn.addEventListener('click', () => {
    if (authManager.isAuthenticated()) {
      window.location.href = '/carrito.html';
    } else {
      console.warn('Usuario no autenticado, redirigiendo al login...');
      authManager.redirectToLogin();
    }
  });
}

document.addEventListener('DOMContentLoaded', () => {
  actualizarCantidadCarrito();
});
