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

      if (!Array.isArray(carritoData)) {
        console.warn('Respuesta no es array, asignando []');
        this.cart = [];
      } else {
        this.cart = carritoData.map(item => ({
          id: item.id,  // Usar id del carrito (registro en tabla carrito)
          nombre: item.nombre || 'Producto sin nombre',
          cantidad: item.cantidad || 1,
          precio: item.precioUnitario || 0,
          precioTotal: item.precioTotal || 0,
          esDigital: item.esDigital ?? false,
          linkArchivo: item.linkArchivo || null
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
      <td><span class="price">$${item.precio.toFixed(2)}</span></td>
      
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

      <td><span class="price">$${(item.precio * item.cantidad).toFixed(2)}</span></td>
      
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
    return;
  }

  // Limitar máximo si querés
  if (nuevaCantidad > 99) {
    cantidadCambio = 99 - item.cantidad;
  }

  try {
    console.log("Sumando cantidad:", { carritoId, cantidadCambio });
    // Enviar solo el delta al backend
    await window.API.sumarCantidad(carritoId, cantidadCambio);

    // Actualizar localmente
    item.cantidad += cantidadCambio;
    this.saveCart();
    this.renderCart();
    this.updateCartCount();

  } catch (error) {
    console.error('Error actualizando cantidad en el servidor:', error);
    alert('No se pudo actualizar la cantidad, intente nuevamente.');
  }
}


 async removeItem(carritoId) {
  if (!confirm('¿Estás seguro de que quieres eliminar este producto del carrito?')) return;

  try {
    await window.API.borrarProductoCarrito(carritoId);
    this.cart = this.cart.filter(item => item.id !== carritoId);
    this.saveCart();
    this.renderCart();
    this.updateCartCount();
  } catch (error) {
    // Mostrar error detallado
    alert(`No se pudo eliminar el producto, intente nuevamente.\nDetalle: ${error.message || error}`);
    console.log('Error en removeItem:', error);
  }
}

async clearCart() {
  if (!confirm('¿Estás seguro de que quieres vaciar todo el carrito?')) return;

  try {
    await window.API.vaciarCarrito();
    this.cart = [];
    this.saveCart();
    this.renderCart();
    this.updateCartCount();
    alert('Carrito vaciado exitosamente');
  } catch (error) {
    alert(`No se pudo vaciar el carrito, intente nuevamente.\nDetalle: ${error.message || error}`);
    console.log('Error en clearCart:', error);
  }
}



updateSummary() {
    const subtotal = this.cart.reduce((sum, item) => sum + (item.precio * item.cantidad), 0);
    const total = subtotal;

    document.getElementById('subtotal').textContent = `$${subtotal.toFixed(2)}`;
    document.getElementById('shipping').textContent = '-';  // o vacío
    document.getElementById('taxes').textContent = '-';    // o vacío
    document.getElementById('total').textContent = `$${total.toFixed(2)}`;
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

        // Here you would typically redirect to a payment page
        // For now, we'll show an alert
        const total = this.cart.reduce((sum, item) => sum + (item.precio * item.quantity), 0);
        const shipping = total > 50 ? 0 : 5.99;
        const taxes = total * 0.08;
        const finalTotal = total + shipping + taxes;

        
        // Redirect to payment page (to be implemented)
        window.location.href = '/WEB/confirmar-pedido.html';
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
        if (category.id === 1) return; // 👈 Saltar la categoría con id 1
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
            window.location.href = `/WEB/categoria.html?categoria=${encodeURIComponent(categoryName)}`;
        }
    }

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