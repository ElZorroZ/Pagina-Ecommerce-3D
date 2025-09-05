class CheckoutManager {
    constructor() {
        this.userInfo = {
            nombre: '',
            apellido: '',
            email: '',
            telefono: '',
            direccion: '',
            cp: '',
            ciudad: ''
        };
        this.cart = [];
        this.isEditingPersonal = false;
        this.isEditingShipping = false;
    }

    async init() {
        this.showLoading();
        
        try {
            // Load user information and cart data
            await this.loadUserInfo();
            await this.loadCartData();
            
            this.renderUserInfo();
            this.renderShippingInfo();
            this.renderOrderSummary();
            this.bindEvents();
            
            this.hideLoading();
        } catch (error) {
            console.error('Error initializing checkout:', error);
            this.hideLoading();
            this.showError('Error al cargar la información del pedido');
        }
    }

    showLoading() {
        document.getElementById('loading').classList.remove('hidden');
        document.getElementById('checkout-content').classList.add('hidden');
    }

    hideLoading() {
        document.getElementById('loading').classList.add('hidden');
        document.getElementById('checkout-content').classList.remove('hidden');
    }

    showError(message) {
        // Simple error handling - in a real app, you'd want a proper error modal
        alert(message);
    }

    async loadUserInfo() {
        try {
            // Simulate API call to get user information
            // Replace this with actual API call
            const simulatedUserData = {
                nombre: 'Juan',
                apellido: 'Pérez',
                email: 'juan.perez@email.com',
                telefono: '+54 11 1234-5678',
                direccion: '', // Empty to show editing functionality
                cp: '',
                ciudad: ''
            };

            // Simulate loading delay
            await new Promise(resolve => setTimeout(resolve, 800));
            
            this.userInfo = { ...simulatedUserData };
        } catch (error) {
            console.error('Error loading user info:', error);
            // Keep default empty values if API fails
        }
    }

    async loadCartData() {
        try {
            // Use existing cart API if available, otherwise simulate
            if (window.API && window.API.obtenerCarrito) {
                const cartData = await window.API.obtenerCarrito();
                this.cart = cartData || [];
            } else {
                // Simulate cart data
                this.cart = [
                    {
                        id: 1,
                        nombre: 'Producto de Ejemplo 1',
                        precio: 2500,
                        cantidad: 2,
                        imagen: 'https://via.placeholder.com/60x60',
                        variante: 'Talle M, Color Azul',
                        tipo: 'fisico'
                    },
                    {
                        id: 2,
                        nombre: 'Producto Digital',
                        precio: 1200,
                        cantidad: 1,
                        imagen: 'https://via.placeholder.com/60x60',
                        variante: 'Descarga inmediata',
                        tipo: 'digital'
                    }
                ];
            }
        } catch (error) {
            console.error('Error loading cart:', error);
            this.cart = [];
        }
    }

    renderUserInfo() {
        document.getElementById('display-nombre').textContent = this.userInfo.nombre || 'No especificado';
        document.getElementById('display-apellido').textContent = this.userInfo.apellido || 'No especificado';
        document.getElementById('display-email').textContent = this.userInfo.email || 'No especificado';
        document.getElementById('display-telefono').textContent = this.userInfo.telefono || 'No especificado';

        // Pre-fill form fields
        document.getElementById('nombre').value = this.userInfo.nombre;
        document.getElementById('apellido').value = this.userInfo.apellido;
        document.getElementById('email').value = this.userInfo.email;
        document.getElementById('telefono').value = this.userInfo.telefono;
    }

    renderShippingInfo() {
        document.getElementById('display-direccion').textContent = this.userInfo.direccion || 'No especificada';
        document.getElementById('display-cp').textContent = this.userInfo.cp || 'No especificado';
        document.getElementById('display-ciudad').textContent = this.userInfo.ciudad || 'No especificada';

        // Pre-fill form fields
        document.getElementById('direccion').value = this.userInfo.direccion;
        document.getElementById('cp').value = this.userInfo.cp;
        document.getElementById('ciudad').value = this.userInfo.ciudad;
    }

    renderOrderSummary() {
        const orderItemsContainer = document.getElementById('order-items');
        orderItemsContainer.innerHTML = '';

        if (this.cart.length === 0) {
            orderItemsContainer.innerHTML = '<p class="empty-cart">No hay productos en el carrito</p>';
            return;
        }

        let subtotal = 0;

        this.cart.forEach(item => {
            const itemTotal = item.precio * item.cantidad;
            subtotal += itemTotal;

            const orderItem = document.createElement('div');
            orderItem.className = 'order-item';
            orderItem.innerHTML = `
                <img src="${item.imagen}" alt="${item.nombre}" class="item-image">
                <div class="item-details">
                    <div class="item-name">${item.nombre}</div>
                    <div class="item-variant">${item.variante}</div>
                    <div class="item-quantity">Cantidad: ${item.cantidad}</div>
                </div>
                <div class="item-price">$${itemTotal.toLocaleString()}</div>
            `;
            orderItemsContainer.appendChild(orderItem);
        });

        // Calculate shipping (free for digital products, $500 for physical)
        const hasPhysicalProducts = this.cart.some(item => item.tipo === 'fisico');
        const shippingCost = hasPhysicalProducts ? 500 : 0;
        const total = subtotal + shippingCost;

        // Update summary
        document.getElementById('subtotal').textContent = `$${subtotal.toLocaleString()}`;
        document.getElementById('shipping-cost').textContent = shippingCost > 0 ? `$${shippingCost.toLocaleString()}` : 'Gratis';
        document.getElementById('total').textContent = `$${total.toLocaleString()}`;
    }

    bindEvents() {
        // Personal info editing
        document.getElementById('edit-personal-btn').addEventListener('click', () => {
            this.togglePersonalEdit(true);
        });

        document.getElementById('cancel-edit-btn').addEventListener('click', () => {
            this.togglePersonalEdit(false);
        });

        document.getElementById('user-info-form').addEventListener('submit', (e) => {
            e.preventDefault();
            this.savePersonalInfo();
        });

        // Shipping info editing
        document.getElementById('edit-shipping-btn').addEventListener('click', () => {
            this.toggleShippingEdit(true);
        });

        document.getElementById('cancel-shipping-btn').addEventListener('click', () => {
            this.toggleShippingEdit(false);
        });

        document.getElementById('shipping-info-form').addEventListener('submit', (e) => {
            e.preventDefault();
            this.saveShippingInfo();
        });

        // Order confirmation
        document.getElementById('confirm-order-btn').addEventListener('click', () => {
            this.confirmOrder();
        });
    }

    togglePersonalEdit(editing) {
        this.isEditingPersonal = editing;
        const displayDiv = document.getElementById('user-info-display');
        const formDiv = document.getElementById('user-info-form');

        if (editing) {
            displayDiv.classList.add('hidden');
            formDiv.classList.remove('hidden');
            document.getElementById('nombre').focus();
        } else {
            displayDiv.classList.remove('hidden');
            formDiv.classList.add('hidden');
            // Reset form to original values
            this.renderUserInfo();
        }
    }

    toggleShippingEdit(editing) {
        this.isEditingShipping = editing;
        const displayDiv = document.getElementById('shipping-info-display');
        const formDiv = document.getElementById('shipping-info-form');

        if (editing) {
            displayDiv.classList.add('hidden');
            formDiv.classList.remove('hidden');
            document.getElementById('direccion').focus();
        } else {
            displayDiv.classList.remove('hidden');
            formDiv.classList.add('hidden');
            // Reset form to original values
            this.renderShippingInfo();
        }
    }

    async savePersonalInfo() {
        const formData = new FormData(document.getElementById('user-info-form'));
        
        // Update local data
        this.userInfo.nombre = formData.get('nombre');
        this.userInfo.apellido = formData.get('apellido');
        this.userInfo.email = formData.get('email');
        this.userInfo.telefono = formData.get('telefono');

        try {
            // Simulate API call to save user info
            console.log('Saving user info:', this.userInfo);
            await new Promise(resolve => setTimeout(resolve, 500));
            
            // Update display and exit edit mode
            this.renderUserInfo();
            this.togglePersonalEdit(false);
            
            // Show success feedback
            this.showSuccessMessage('Información personal actualizada');
        } catch (error) {
            console.error('Error saving user info:', error);
            this.showError('Error al guardar la información personal');
        }
    }

    async saveShippingInfo() {
        const formData = new FormData(document.getElementById('shipping-info-form'));
        
        // Update local data
        this.userInfo.direccion = formData.get('direccion');
        this.userInfo.cp = formData.get('cp');
        this.userInfo.ciudad = formData.get('ciudad');

        try {
            // Simulate API call to save shipping info
            console.log('Saving shipping info:', this.userInfo);
            await new Promise(resolve => setTimeout(resolve, 500));
            
            // Update display and exit edit mode
            this.renderShippingInfo();
            this.toggleShippingEdit(false);
            
            // Show success feedback
            this.showSuccessMessage('Información de envío actualizada');
        } catch (error) {
            console.error('Error saving shipping info:', error);
            this.showError('Error al guardar la información de envío');
        }
    }

    showSuccessMessage(message) {
        // Simple success feedback - in a real app, you'd want a toast notification
        const originalBtn = document.getElementById('edit-personal-btn');
        const originalText = originalBtn.innerHTML;
        originalBtn.innerHTML = '✓ Guardado';
        originalBtn.style.color = 'green';
        
        setTimeout(() => {
            originalBtn.innerHTML = originalText;
            originalBtn.style.color = '';
        }, 2000);
    }

    async confirmOrder() {
        // Validate required information
        if (!this.validateOrderData()) {
            return;
        }

        const confirmBtn = document.getElementById('confirm-order-btn');
        const originalText = confirmBtn.innerHTML;
        
        // Show loading state
        confirmBtn.innerHTML = `
            <div style="width: 20px; height: 20px; border: 2px solid transparent; border-top: 2px solid currentColor; border-radius: 50%; animation: spin 1s linear infinite;"></div>
            Procesando...
        `;
        confirmBtn.disabled = true;

        try {
            // Get selected payment method
            const paymentMethod = document.querySelector('input[name="payment-method"]:checked').value;
            
            // Simulate order processing
            await this.processOrder(paymentMethod);
            
            // Show success modal
            this.showSuccessModal(paymentMethod);
            
        } catch (error) {
            console.error('Error confirming order:', error);
            this.showError('Error al procesar el pedido. Por favor, intenta nuevamente.');
            
            // Reset button
            confirmBtn.innerHTML = originalText;
            confirmBtn.disabled = false;
        }
    }

    validateOrderData() {
        // Check required personal info
        if (!this.userInfo.nombre || !this.userInfo.apellido || !this.userInfo.email || !this.userInfo.telefono) {
            this.showError('Por favor completa toda la información personal requerida');
            return false;
        }

        // Check shipping info for physical products
        const hasPhysicalProducts = this.cart.some(item => item.tipo === 'fisico');
        if (hasPhysicalProducts && (!this.userInfo.direccion || !this.userInfo.cp || !this.userInfo.ciudad)) {
            this.showError('Por favor completa la información de envío para productos físicos');
            return false;
        }

        return true;
    }

    async processOrder(paymentMethod) {
        const orderData = {
            userInfo: this.userInfo,
            cart: this.cart,
            paymentMethod: paymentMethod,
            timestamp: new Date().toISOString(),
            orderId: this.generateOrderId()
        };

        console.log('Processing order:', orderData);

        // Simulate different payment processing times
        const processingTime = paymentMethod === 'mercadopago' ? 2000 : 1500;
        await new Promise(resolve => setTimeout(resolve, processingTime));

        // Here you would integrate with actual payment APIs:
        // - Mercado Pago SDK for credit card payments
        // - Bank transfer API for bank transfers
        
        return orderData;
    }

    generateOrderId() {
        return 'ORD-' + Date.now() + '-' + Math.random().toString(36).substr(2, 5).toUpperCase();
    }

    showSuccessModal(paymentMethod) {
        const modal = document.getElementById('success-modal');
        const subtotal = this.cart.reduce((sum, item) => sum + (item.precio * item.cantidad), 0);
        const hasPhysicalProducts = this.cart.some(item => item.tipo === 'fisico');
        const shippingCost = hasPhysicalProducts ? 500 : 0;
        const total = subtotal + shippingCost;

        // Update modal content
        document.getElementById('order-number').textContent = this.generateOrderId();
        document.getElementById('order-total').textContent = `$${total.toLocaleString()}`;
        
        const paymentMethodNames = {
            'mercadopago': 'Mercado Pago',
            'transferencia': 'Transferencia Bancaria'
        };
        document.getElementById('payment-method-selected').textContent = paymentMethodNames[paymentMethod];

        modal.classList.remove('hidden');

        // Clear cart after successful order (simulate)
        setTimeout(() => {
            this.clearCart();
        }, 1000);
    }

    async clearCart() {
        try {
            // Clear cart via API if available
            if (window.API && window.API.vaciarCarrito) {
                await window.API.vaciarCarrito();
            }
            
            // Clear local cart
            this.cart = [];
            console.log('Cart cleared after successful order');
        } catch (error) {
            console.error('Error clearing cart:', error);
        }
    }
}

// Utility functions for categories and navigation (from existing pattern)
async function loadCategories() {
    try {
        if (window.API && window.API.getCategories) {
            const categories = await window.API.getCategories();
            if (categories) {
                renderCategories(categories);
            }
        }
    } catch (error) {
        console.error('Error loading categories:', error);
    }
}

function renderCategories(categories) {
    const dropdown = document.querySelector('#categories-dropdown .dropdown-content');
    if (!dropdown) return;

    dropdown.innerHTML = categories.map(category => 
        `<a href="/WEB/category.html?id=${category.id}" class="dropdown-item">${category.nombre}</a>`
    ).join('');
}

function handleClicks(e) {
    if (e.target.matches('.dropdown-item')) {
        e.preventDefault();
        const url = e.target.getAttribute('href');
        if (url) {
            window.location.href = url;
        }
    }
}

function initializeDropdown() {
    const shopTrigger = document.getElementById('shop-trigger');
    const dropdown = document.getElementById('categories-dropdown');
    
    if (shopTrigger && dropdown) {
        shopTrigger.addEventListener('mouseenter', () => {
            dropdown.style.display = 'block';
        });
        
        shopTrigger.parentElement.addEventListener('mouseleave', () => {
            dropdown.style.display = 'none';
        });
    }
}

function initializeMobileMenu() {
    const mobileMenuBtn = document.getElementById('mobile-menu-btn');
    const mobileMenu = document.getElementById('mobile-menu');
    
    if (mobileMenuBtn && mobileMenu) {
        mobileMenuBtn.addEventListener('click', () => {
            mobileMenu.classList.toggle('hidden');
        });
    }
}

// Initialize checkout when DOM is loaded
document.addEventListener('DOMContentLoaded', async () => {
    const checkoutManager = new CheckoutManager();
    await checkoutManager.init();
    
    // Initialize navigation components
    initializeDropdown();
    initializeMobileMenu();
    loadCategories();
    document.addEventListener('click', handleClicks);
});
