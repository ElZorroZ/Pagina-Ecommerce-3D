// Cart management
class CartManager {
    constructor() {
        this.cart = this.loadCart();
        this.init();
    }
    
    init() {
        this.renderCart();
        this.updateCartCount();
        this.bindEvents();
    }

    loadCart() {
        const saved = localStorage.getItem('cart');
        return saved ? JSON.parse(saved) : [];
    }

    saveCart() {
        localStorage.setItem('cart', JSON.stringify(this.cart));
    }

    bindEvents() {
        // Checkout button
        document.getElementById('checkout-btn')?.addEventListener('click', () => {
            this.proceedToCheckout();
        });

        // Update cart count in header
        this.updateCartCount();
    }

    renderCart() {
        const cartItemsContainer = document.getElementById('cart-items');
        const emptyCart = document.getElementById('empty-cart');
        const cartSummary = document.getElementById('cart-summary');

        if (this.cart.length === 0) {
            emptyCart.style.display = 'block';
            cartSummary.style.display = 'none';
            cartItemsContainer.innerHTML = '';
            return;
        }

        emptyCart.style.display = 'none';
        cartSummary.style.display = 'block';

        cartItemsContainer.innerHTML = this.cart.map(item => `
            <tr data-product-id="${item.id}">
                <td>
                    <div class="product-info">
                        <img src="${item.imagen || '/api/placeholder/80/80'}" 
                             alt="${item.nombre}" 
                             class="product-image-cart">
                        <div class="product-details">
                            <div class="product-name">${item.nombre}</div>
                            <div class="product-variant">${item.color || 'Color por defecto'}</div>
                        </div>
                    </div>
                </td>
                <td>
                    <span class="price">$${item.precio}</span>
                </td>
                <td>
                    <div class="quantity-controls">
                        <button class="quantity-btn" onclick="cartManager.updateQuantity(${item.id}, ${item.quantity - 1})" ${item.quantity <= 1 ? 'disabled' : ''}>
                            −
                        </button>
                        <input type="number" 
                               value="${item.quantity}" 
                               min="1" 
                               max="99"
                               class="quantity-input"
                               onchange="cartManager.updateQuantity(${item.id}, this.value)">
                        <button class="quantity-btn" onclick="cartManager.updateQuantity(${item.id}, ${item.quantity + 1})">
                            +
                        </button>
                    </div>
                </td>
                <td>
                    <span class="price">$${(item.precio * item.quantity).toFixed(2)}</span>
                </td>
                <td>
                    <button class="remove-btn" onclick="cartManager.removeItem(${item.id})" title="Eliminar producto">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <polyline points="3,6 5,6 21,6"></polyline>
                            <path d="m19,6v14a2,2 0 0,1 -2,2H7a2,2 0 0,1 -2,-2V6m3,0V4a2,2 0 0,1 2,-2h4a2,2 0 0,1 2,2v2"></path>
                            <line x1="10" y1="11" x2="10" y2="17"></line>
                            <line x1="14" y1="11" x2="14" y2="17"></line>
                        </svg>
                    </button>
                </td>
            </tr>
        `).join('');

        this.updateSummary();
    }

    updateQuantity(productId, newQuantity) {
        newQuantity = parseInt(newQuantity);
        
        if (newQuantity < 1) {
            this.removeItem(productId);
            return;
        }

        if (newQuantity > 99) {
            newQuantity = 99;
        }

        const item = this.cart.find(item => item.id === productId);
        if (item) {
            item.quantity = newQuantity;
            this.saveCart();
            this.renderCart();
            this.updateCartCount();
        }
    }

    removeItem(productId) {
        if (confirm('¿Estás seguro de que quieres eliminar este producto del carrito?')) {
            this.cart = this.cart.filter(item => item.id !== productId);
            this.saveCart();
            this.renderCart();
            this.updateCartCount();
        }
    }

    updateSummary() {
        const subtotal = this.cart.reduce((sum, item) => sum + (item.precio * item.quantity), 0);
        const shipping = subtotal > 50 ? 0 : 5.99; // Free shipping over $50
        const taxRate = 0.08; // 8% tax
        const taxes = subtotal * taxRate;
        const total = subtotal + shipping + taxes;

        document.getElementById('subtotal').textContent = `$${subtotal.toFixed(2)}`;
        document.getElementById('shipping').textContent = shipping === 0 ? 'Gratis' : `$${shipping.toFixed(2)}`;
        document.getElementById('taxes').textContent = `$${taxes.toFixed(2)}`;
        document.getElementById('total').textContent = `$${total.toFixed(2)}`;
    }

    updateCartCount() {
        const count = this.cart.reduce((sum, item) => sum + item.quantity, 0);
        const cartCountElement = document.getElementById('cart-count');
        if (cartCountElement) {
            cartCountElement.textContent = count;
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

        alert(`Procesando pago por: $${finalTotal.toFixed(2)}\n\nEsta funcionalidad será implementada próximamente.`);
        
        // Redirect to payment page (to be implemented)
        // window.location.href = 'checkout.html';
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
            window.location.href = `/categoria.html?categoria=${encodeURIComponent(categoryName)}`;
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

// Initialize cart manager and other functionality
let cartManager;

document.addEventListener('DOMContentLoaded', () => {
    cartManager = new CartManager();
    initializeMobileMenu();
    initializeDropdown();
    loadCategories();
    document.addEventListener('click', handleClicks);
});