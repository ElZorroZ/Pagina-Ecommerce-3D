// Cart management
class CartManager {
    constructor() {
            this.cart = [];  // Inicialmente vacío

    }
    
   async init() {
      await this.loadCart();
        this.renderCart();
        this.updateCartCount();
        this.bindEvents();
    }

  async loadCart() {
    try {
      const carritoData = await window.API.obtenerCarrito();
      console.log("Estructura", carritoData);

      if (!Array.isArray(carritoData)) {
        console.warn('Respuesta no es array, asignando []');
        this.cart = [];
      } else {
        this.cart = carritoData.map(item => ({
          id: item.id,  // id del carrito
          nombre: item.nombre || 'Producto sin nombre',
          cantidad: item.cantidad || 1,
          precio: item.precioUnitario || 0,
          precioTotal: item.precioTotal || 0,
          esDigital: item.esDigital ?? false,
          linkArchivo: item.linkArchivo || null,
          colorNombre: item.colorNombre || 'Sin color' // <--- agregado
        }));
      }
    } catch (error) {
      console.error('Error cargando carrito:', error);
      this.cart = [];
    }
}
    saveCart() {
        localStorage.setItem('cart', JSON.stringify(this.cart));
    }

    bindEvents() {
        // Checkout button
        document.getElementById('checkout-btn')?.addEventListener('click', () => {
            this.proceedToCheckout();
        });

        // Clear cart button
        document.getElementById('clear-cart-btn')?.addEventListener('click', () => {
            this.clearCart();
        });

        // Update cart count in header
        this.updateCartCount();
    }

renderCart() {
  const cartItemsContainer = document.getElementById('cart-items');
  const emptyCart = document.getElementById('empty-cart');
  const cartSummary = document.getElementById('cart-summary');
  const clearCartContainer = document.getElementById('clear-cart-container');

  if (!this.cart || this.cart.length === 0) {
    emptyCart.style.display = 'block';
    cartSummary.style.display = 'none';
    clearCartContainer.style.display = 'none';
    cartItemsContainer.innerHTML = '';
    return;
  }

  // Eliminar duplicados por id
  this.cart = this.cart.filter((item, index, self) =>
    index === self.findIndex((t) => t.id === item.id)
  );

  emptyCart.style.display = 'none';
  cartSummary.style.display = 'block';
  clearCartContainer.style.display = 'block';

cartItemsContainer.innerHTML = this.cart.map(item => {
  const imagen = item.linkArchivo ? item.linkArchivo : '/api/placeholder/80/80';
  const colorTexto = item.esDigital ? '' : (item.colorNombre || 'Sin color');

  return `
    <tr data-cart-id="${item.id}">
      <td>
        <div class="product-info">
          <img src="${imagen}" alt="${item.nombre}" class="product-image-cart">
          <div class="product-details">
            <div class="product-name">${item.nombre}</div>
          </div>
        </div>
      </td>

      <td>${colorTexto}</td> <!-- Nueva columna color -->

      <td><span class="price">${formatPrice(item.precio)}</span></td>
      
      <td>
        ${
          item.esDigital
            ? '<span class="digital-product">Digital</span>'
            : `
              <div class="quantity-controls">
                <button class="quantity-btn" onclick="cartManager.updateQuantity(${item.id}, -1)" ${item.cantidad <= 1 ? 'disabled' : ''}>−</button>
                <input
                  type="number"
                  value="${item.cantidad}"
                  min="1"
                  max="99"
                  class="quantity-input"
                  onchange="cartManager.updateQuantity(${item.id}, this.value - ${item.cantidad})"
                />
                <button class="quantity-btn" onclick="cartManager.updateQuantity(${item.id}, 1)">+</button>
              </div>
            `
        }
      </td>

      <td><span class="price">${formatPrice(item.precio * item.cantidad)}</span></td>
      
      <td>
        <button class="remove-btn" onclick="cartManager.removeItem(${item.id})" title="Eliminar producto">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="3 6 5 6 21 6"></polyline>
            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6"></path>
            <line x1="10" y1="11" x2="10" y2="17"></line>
            <line x1="14" y1="11" x2="14" y2="17"></line>
            <path d="M9 6V4a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2v2"></path>
          </svg>
        </button>
      </td>
    </tr>
  `;
}).join('');
  console.log("Carrito sin duplicados:", this.cart);

  this.updateSummary();
}

async updateQuantity(carritoId, cantidadCambio) {
  cantidadCambio = parseInt(cantidadCambio);

  // Encontrar el item para saber la cantidad actual
  const item = this.cart.find(item => item.id === carritoId);
  if (!item) return;

  const nuevaCantidad = item.cantidad + cantidadCambio;

  // Validar que nueva cantidad sea >= 1
  if (nuevaCantidad < 1) {
    this.removeItem(carritoId);
    await actualizarCantidadCarrito(); // actualizar contador
    return;
  }

  // Limitar máximo si querés
  if (nuevaCantidad > 99) {
    cantidadCambio = 99 - item.cantidad;
  }

    try {
      mostrarCarga("Actualizando cantidad...");
      console.log("Sumando cantidad:", { carritoId, cantidadCambio });

      // Enviar solo el delta al backend
      await window.API.sumarCantidad(carritoId, cantidadCambio);

      // Actualizar localmente
      item.cantidad += cantidadCambio;
      this.saveCart();
      this.renderCart();

      // 🔹 Actualizar contador local
      await actualizarCantidadCarrito();

      mostrarExito("Cantidad actualizada correctamente"); // Mensaje de éxito

  } catch (error) {
      console.error('Error actualizando cantidad en el servidor:', error);
      mostrarError("No se pudo actualizar la cantidad, intente nuevamente."); // Mensaje de error
  } finally {
      // Ocultar overlay siempre
      ocultarCarga();
  }

}


 async removeItem(carritoId) {
  // Usamos la función de confirmación atractiva
  mostrarConfirmacion('¿Estás seguro de que quieres eliminar este producto del carrito?', async (confirmado) => {
    if (!confirmado) return;

    try {
      mostrarCarga("Eliminando producto..."); // Mostrar overlay

      await window.API.borrarProductoCarrito(carritoId);
      
      // Filtramos el item eliminado
      this.cart = this.cart.filter(item => item.id !== carritoId);
      
      this.saveCart();
      this.renderCart();
      this.updateCartCount();

      // Mostrar mensaje de éxito
      mostrarExito('Producto eliminado del carrito correctamente.');
    } catch (error) {
      // Mostrar mensaje de error
      mostrarError(`No se pudo eliminar el producto. Detalle: ${error.message || error}`);
      console.log('Error en removeItem:', error);
    } finally {
      ocultarCarga(); // Ocultar overlay siempre
    }
  });
}


async clearCart() {
    const confirmado = await new Promise(resolve => {
        mostrarConfirmacion('¿Estás seguro de que quieres vaciar todo el carrito?', resolve);
    });

    if (!confirmado) return;

    try {
        mostrarCarga("Vaciando carrito..."); // Mostrar overlay
        await window.API.vaciarCarrito();

        this.cart = [];
        this.saveCart();
        this.renderCart();
        this.updateCartCount();

        mostrarExito('Carrito vaciado exitosamente');
    } catch (error) {
        mostrarError(`No se pudo vaciar el carrito, intente nuevamente.\nDetalle: ${error.message || error}`);
        console.log('Error en clearCart:', error);
    } finally {
        ocultarCarga(); // Ocultar overlay siempre
    }
}

updateSummary() {
    const subtotal = this.cart.reduce((sum, item) => sum + (item.precio * item.cantidad), 0);
    const total = subtotal;

    document.getElementById('subtotal').textContent = formatPrice(subtotal);
    document.getElementById('shipping').textContent = '-';  
    document.getElementById('taxes').textContent = '-';    
    document.getElementById('total').textContent = formatPrice(total);
}



   async updateCartCount() {
    try {
        // Llamar a la API para obtener el carrito actualizado
        const carritoData = await window.API.obtenerCarrito();

        // Sumar las cantidades
        const count = carritoData.reduce((sum, item) => sum + item.cantidad, 0);

        const cartCountElement = document.getElementById('cart-count');
        if (cartCountElement) {
            cartCountElement.textContent = count;
        }
    } catch (error) {
        console.error('Error actualizando contador de carrito:', error);
    }
}


    proceedToCheckout() {
    if (this.cart.length === 0) {
        alert('Tu carrito está vacío');
        return;
    }

    // Agrupar productos por nombre, igual que en renderOrderSummary
    const grouped = {};
    this.cart.forEach(item => {
        const key = item.nombre;
        if (!grouped[key]) {
            grouped[key] = {
                ...item,
                cantidadTotal: Number(item.cantidad),
                precioUnitario: Number(item.precioUnitario ?? item.precio),
            };
        } else {
            grouped[key].cantidadTotal += Number(item.cantidad);
        }
    });

    // Calcular subtotal
    let subtotal = 0;
    Object.values(grouped).forEach(item => {
        subtotal += item.precioUnitario * item.cantidadTotal;
    });

    // Detectar productos físicos
    const hasPhysicalProducts = Object.values(grouped).some(item => !item.linkArchivo);
    const shipping = hasPhysicalProducts ? 500 : 0;

    const total = subtotal + shipping;

    console.log('Subtotal:', subtotal);
    console.log('Shipping:', shipping);
    console.log('Total:', total);

    // Guardar en sessionStorage o localStorage para la página de confirmar-pedido
    sessionStorage.setItem('checkoutSubtotal', subtotal);
    sessionStorage.setItem('checkoutShipping', shipping);
    sessionStorage.setItem('checkoutTotal', total);

    window.location.href = '/confirmar-pedido.html';
}

}

// Load categories from API
async function loadCategories() {
    const categories = await API.getCategories();
    renderCategories(categories);
}

// Render categories in dropdown
function renderCategories(categories) {
    if (!Array.isArray(categories)) {
        console.error('Categorías inválidas:', categories);
        return;
    }

    categoriesDropdown.innerHTML = '';
    
    categories.forEach(category => {
        const categoryLink = document.createElement('a');
        categoryLink.href = '#';
        categoryLink.className = 'dropdown-category';
        categoryLink.textContent = category.nombre; // "nombre" según tu DTO
        categoryLink.dataset.categoryId = category.id;
        
        categoriesDropdown.appendChild(categoryLink);
    });
}
// Handle all click events
function handleClicks(e) {
    // Handle category clicks in dropdown
    if (e.target.classList.contains('dropdown-category')) {
        e.preventDefault();
        const categoryId = e.target.dataset.categoryId;
        const categoryName = e.target.textContent.toLowerCase().replace(/ /g, '-');
        if (categoryId) {
            window.location.href = `/categoria.html?categoria=${encodeURIComponent(categoryName)}`;
        }
    }

}
function formatPrice(price) {
    return new Intl.NumberFormat('es-CL', {
        style: 'currency',
        currency: 'CLP'
    }).format(price);
}

const categoriesDropdown = document.getElementById('categories-dropdown');

// Initialize shop dropdown functionality
function initializeDropdown() {
    const shopTrigger = document.getElementById('shop-trigger');
    const categoriesDropdownMenu = document.getElementById('categories-dropdown');

    if (shopTrigger && categoriesDropdownMenu) {
        // Show dropdown on hover
        shopTrigger.addEventListener('mouseenter', () => {
            categoriesDropdownMenu.classList.add('show');
        });
        
        // Hide dropdown when leaving the entire dropdown area
        const navDropdown = shopTrigger.parentElement;
        navDropdown.addEventListener('mouseleave', () => {
            categoriesDropdownMenu.classList.remove('show');
        });
    }
}

// Mobile menu functionality
function initializeMobileMenu() {
    const mobileMenuBtn = document.getElementById('mobile-menu-btn');
    const mobileNav = document.querySelector('.mobile-nav');

    if (mobileMenuBtn && mobileNav) {
        mobileMenuBtn.addEventListener('click', () => {
            mobileNav.classList.toggle('show');
        });
    }
}



document.addEventListener('DOMContentLoaded', async () => {
    cartManager = new CartManager();
    await cartManager.init();
    initializeMobileMenu();
    initializeDropdown();
    loadCategories();
    document.addEventListener('click', handleClicks);
    
});