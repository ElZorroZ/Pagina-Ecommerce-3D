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
        const token = localStorage.getItem('accessToken');
        if (!token) throw new Error('No se encontró token de acceso');

        // Decodificar JWT para obtener sub (gmail)
        const payloadBase64 = token.split('.')[1];
        const payloadJson = atob(payloadBase64.replace(/-/g, '+').replace(/_/g, '/'));
        const payload = JSON.parse(payloadJson);
        const gmail = payload.sub;

        // Llamada a la API
        const data = await window.API.obtenerUsuarioPorToken();

        // Asignar userInfo, asegurándose de que email sea el gmail del token
        this.userInfo = { 
            ...data, 
            email: gmail || data.email || '' 
        };
    } catch (error) {
        console.error('Error loading user info:', error);
        this.userInfo = {
            nombre: '',
            apellido: '',
            email: localStorage.getItem('accessToken') 
                   ? JSON.parse(atob(localStorage.getItem('accessToken').split('.')[1])).sub 
                   : '',
            telefono: '',
            direccion: '',
            cp: '',
            ciudad: ''
        };
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

async renderOrderSummary() {
    const orderItemsContainer = document.getElementById('order-items');
    orderItemsContainer.innerHTML = '';

    try {
        this.cart = await window.API.obtenerCarrito();
        console.log("Carrito completo:", this.cart);

        if (this.cart.length === 0) {
            orderItemsContainer.innerHTML = '<p class="empty-cart">No hay productos en el carrito</p>';
            document.getElementById('subtotal').textContent = '$0';
            document.getElementById('shipping-cost').textContent = 'Gratis';
            document.getElementById('total').textContent = '$0';

            // Guardar en sessionStorage
            sessionStorage.setItem('checkoutSubtotal', 0);
            sessionStorage.setItem('checkoutShipping', 0);
            sessionStorage.setItem('checkoutTotal', 0);
            return;
        }

        // Agrupar productos por nombre
        const grouped = {};
        this.cart.forEach(item => {
            const key = item.nombre;
            const cantidad = Number(item.cantidad);
            const precioUnitario = Number(item.precioUnitario ?? item.precio ?? 0);

            if (!grouped[key]) {
                grouped[key] = {
                    ...item,
                    cantidadTotal: cantidad,
                    precioUnitario: precioUnitario,
                    linkArchivo: item.linkArchivo
                };
            } else {
                grouped[key].cantidadTotal += cantidad;
            }
        });

        let subtotal = 0;

        Object.values(grouped).forEach(item => {
            const itemTotal = item.precioUnitario * item.cantidadTotal;
            subtotal += itemTotal;

            const orderItem = document.createElement('div');
            orderItem.className = 'order-item';
            orderItem.innerHTML = `
                ${item.linkArchivo ? `<img src="${item.linkArchivo}" alt="${item.nombre}" class="item-image">` : ''}
                <div class="item-details">
                    <div class="item-name">${item.nombre}</div>
                    <div class="item-quantity">Cantidad: ${item.cantidadTotal}</div>
                </div>
                <div class="item-price">$${itemTotal.toLocaleString()}</div>
            `;
            orderItemsContainer.appendChild(orderItem);
        });

        // Detectar productos físicos
        const hasPhysicalProducts = Object.values(grouped).some(item => !item.linkArchivo);
        const shippingCost = hasPhysicalProducts ? 0 : 0;
        const total = subtotal + shippingCost;

        document.getElementById('subtotal').textContent = `$${subtotal.toLocaleString()}`;
        document.getElementById('total').textContent = `$${total.toLocaleString()}`;

        // Guardar en sessionStorage para que coincida con confirmar-pedido
        sessionStorage.setItem('checkoutSubtotal', subtotal);
        sessionStorage.setItem('checkoutShipping', shippingCost);
        sessionStorage.setItem('checkoutTotal', total);

    } catch (error) {
        console.error('Error cargando el carrito:', error);
        orderItemsContainer.innerHTML = '<p class="empty-cart">No se pudo cargar el carrito</p>';
        document.getElementById('subtotal').textContent = '$0';
        document.getElementById('shipping-cost').textContent = 'Gratis';
        document.getElementById('total').textContent = '$0';

        sessionStorage.setItem('checkoutSubtotal', 0);
        sessionStorage.setItem('checkoutShipping', 0);
        sessionStorage.setItem('checkoutTotal', 0);
    }
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
        
        // Construir objeto de cambios
        const cambios = {
            nombre: formData.get('nombre'),
            apellido: formData.get('apellido'),
            telefono: formData.get('telefono'),
            direccion: formData.get('direccion'),
            cp: formData.get('cp'),
            ciudad: formData.get('ciudad')
        };

        try {
            await window.API.modificarPedido(cambios);

            // Actualizar localmente
            this.userInfo = { ...this.userInfo, ...cambios };

            // Actualizar display y salir del modo edición
            this.renderUserInfo();
            this.togglePersonalEdit(false);

            // Mensaje de éxito
            this.showSuccessMessage('Información personal actualizada');
        } catch (error) {
            console.error('Error saving user info:', error);
            this.showError('Error al guardar la información personal');
        }
    }

async saveShippingInfo() {
    const formData = new FormData(document.getElementById('shipping-info-form'));
    
    // Construir objeto de cambios
    const cambios = {
        direccion: formData.get('direccion'),
        cp: formData.get('cp'),
        ciudad: formData.get('ciudad')
    };

    try {
        // Llamada a la API (ajusta si necesitas un endpoint específico)
        await window.API.modificarPedido(cambios);

        // Actualizar localmente
        this.userInfo = { ...this.userInfo, ...cambios };

        // Actualizar display y salir del modo edición
        this.renderShippingInfo();
        this.toggleShippingEdit(false);

        // Mensaje de éxito
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
    console.log("\n🎬 === INICIANDO confirmOrder ===");
    
    // Ejecutar diagnóstico
    await this.testConnectivity();
    
    if (!this.validateOrderData()) {
        console.log("❌ Validación de datos falló");
        return;
    }

    const confirmBtn = document.getElementById('confirm-order-btn');
    const originalText = confirmBtn.innerHTML;
    const originalDisabled = confirmBtn.disabled;

    // Función para restaurar el botón
    const restoreButton = () => {
        confirmBtn.innerHTML = originalText;
        confirmBtn.disabled = originalDisabled;
        console.log("🔄 Botón restaurado");
    };

    // Función para deshabilitar el botón
    const disableButton = () => {
        confirmBtn.innerHTML = `
            <div style="width: 20px; height: 20px; border: 2px solid transparent; border-top: 2px solid currentColor; border-radius: 50%; animation: spin 1s linear infinite;"></div>
            Procesando...
        `;
        confirmBtn.disabled = true;
        console.log("⏳ Botón deshabilitado");
    };

    disableButton();

    try {
        const paymentMethod = document.querySelector('input[name="payment-method"]:checked');
        
        if (!paymentMethod) {
            throw new Error("Por favor selecciona un método de pago");
        }
        
        const paymentValue = paymentMethod.value;
        console.log("💳 Método de pago seleccionado:", paymentValue);

        // 1️⃣ Crear el pedido en backend
        console.log("📦 Creando pedido...");
        console.log("🛒 Cart:", this.cart);
        
        const pedidoCreado = await window.API.crearPedido(this.cart);
        console.log("✅ Pedido creado:", pedidoCreado);

        // Validaciones del pedido creado
        if (!pedidoCreado) {
            throw new Error("crearPedido devolvió null o undefined");
        }
        if (!pedidoCreado.id) {
            throw new Error("El pedido creado no tiene un ID válido. Pedido completo: " + JSON.stringify(pedidoCreado));
        }
        if (!pedidoCreado.total || pedidoCreado.total <= 0) {
            throw new Error("El pedido creado no tiene un total válido: " + pedidoCreado.total);
        }

        console.log("📋 Estructura del pedido validada:", {
            id: pedidoCreado.id,
            total: pedidoCreado.total,
            fechaPedido: pedidoCreado.fechaPedido,
            estado: pedidoCreado.estado
        });

        // 2️⃣ Si es Mercado Pago → usar API directa
        if (paymentValue === "mercadopago") {
            console.log("💳 Procesando con MercadoPago API directa...");
            
            try {
                // Verificar que MercadoPago SDK esté cargado
                if (typeof MercadoPago === 'undefined') {
                    throw new Error("MercadoPago SDK no está cargado. Verifica que el script esté incluido.");
                }

                // Obtener preferencia del backend
                console.log("🔄 Obteniendo preferencia de pago...");
                const response = await API.confirmarPedido(
                    {
                        id: pedidoCreado.id,
                        total: pedidoCreado.total,
                        fechaPedido: pedidoCreado.fechaPedido,
                        usuarioId: pedidoCreado.usuarioId // 🔹 agregar
                    },
                    1
                );

                console.log("📄 Respuesta del backend:", response);

                let initPoint;
                if (typeof response === 'string') {
                    initPoint = response;
                } else if (response && response.initPoint) {
                    initPoint = response.initPoint;
                } else {
                    throw new Error("Respuesta del backend inválida: " + JSON.stringify(response));
                }

                if (!initPoint) {
                    throw new Error("No se recibió el link de pago de MercadoPago");
                }

                console.log("🎯 Link de pago obtenido:", initPoint);

                // Restaurar botón antes de redireccionar
                restoreButton();
                
                // Pequeña pausa para que se vea la restauración
                await new Promise(resolve => setTimeout(resolve, 10000));

                // Redirigir a MercadoPago
                console.log("🚀 Redirigiendo a MercadoPago...");

                window.location.href = initPoint;
                
                // Si por alguna razón no redirige, restaurar botón
                setTimeout(() => {
                    if (confirmBtn.disabled) {
                        restoreButton();
                        console.log("⚠️ Redirección falló, botón restaurado");
                    }
                }, 3000);

                return;

            } catch (mpError) {
                console.error("❌ Error específico de MercadoPago:", mpError);
                restoreButton();
                this.showError('Error al procesar el pago con MercadoPago: ' + mpError.message);
                return;
            }
        }

        // 3️⃣ Si es otro método de pago
        console.log("💰 Procesando con método:", paymentValue);
        await this.processOrder(paymentValue);
        
        // Restaurar botón
        restoreButton();
        
        // Mostrar éxito
        this.showSuccessModal(paymentValue);

    } catch (error) {
        console.error('\n❌ === ERROR EN confirmOrder ===');
        console.error('Error completo:', error);
        console.error('Error name:', error.name);
        console.error('Error message:', error.message);
        console.error('Error stack:', error.stack);
        console.error('=== FIN ERROR ===\n');
        
        // ✅ SIEMPRE restaurar el botón en caso de error
        restoreButton();
        this.showError('Error al procesar el pedido: ' + error.message);
    }
}

// 🔧 Método auxiliar para verificar el estado de MercadoPago
checkMercadoPagoStatus() {
    console.log("🔍 === DIAGNÓSTICO MERCADOPAGO ===");
    
    // Verificar SDK
    if (typeof MercadoPago === 'undefined') {
        console.error("❌ MercadoPago SDK no cargado");
        return false;
    } else {
        console.log("✅ MercadoPago SDK cargado");
    }

    // Verificar configuración
    console.log("🔑 Public Key configurada:", window.MERCADOPAGO_PUBLIC_KEY ? "✅ Sí" : "❌ No");
    
    return true;
}

// 🔧 Método para inicializar MercadoPago (llamar al cargar la página)
async initializeMercadoPago() {
    try {
        console.log("🚀 Inicializando MercadoPago...");
        
        if (typeof MercadoPago === 'undefined') {
            console.warn("⚠️ MercadoPago SDK no disponible");
            return false;
        }

        // Configurar con tu public key
        if (window.MERCADOPAGO_PUBLIC_KEY) {
            MercadoPago.setPublishableKey(window.MERCADOPAGO_PUBLIC_KEY);
            console.log("✅ MercadoPago inicializado correctamente");
            return true;
        } else {
            console.error("❌ MERCADOPAGO_PUBLIC_KEY no definida");
            return false;
        }
        
    } catch (error) {
        console.error("❌ Error inicializando MercadoPago:", error);
        return false;
    }
}
async testConnectivity() {
    console.log("🔍 DIAGNÓSTICO DE CONECTIVIDAD");
    
    // Test 1: Verificar URL base
    const API_BASE_URL = "https://forma-programada.onrender.com";
    console.log("📍 API_BASE_URL:", API_BASE_URL);
    
    // Test 2: Ping básico al servidor
    try {
        const pingResponse = await fetch(`${API_BASE_URL}/api/health`, { method: 'GET' });
        console.log("🏥 Health check:", pingResponse.status, pingResponse.ok);
    } catch (error) {
        console.error("❌ Health check falló:", error);
    }
    
    // Test 3: Verificar authManager
    console.log("🔐 AuthManager status:");
    console.log("   - isAuthenticated:", authManager.isAuthenticated());
    console.log("   - hasAccessToken:", !!authManager.getAccessToken());
    console.log("   - hasRefreshToken:", !!authManager.getRefreshToken());
    console.log("   - userId:", authManager.getUserId());
    
   
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

    // Para métodos que no son Mercado Pago → simula demora
    if (paymentMethod !== "mercadopago") {
        const processingTime = 1500;
        await new Promise(resolve => setTimeout(resolve, processingTime));
    }

    return orderData;
}



    generateOrderId() {
        return 'ORD-' + Date.now() + '-' + Math.random().toString(36).substr(2, 5).toUpperCase();
    }

    showSuccessModal(paymentMethod) {
        const modal = document.getElementById('success-modal');
        const subtotal = this.cart.reduce((sum, item) => sum + (item.precio * item.cantidad), 0);
        const hasPhysicalProducts = this.cart.some(item => item.tipo === 'fisico');
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
  const categoriesDropdown = document.querySelector("#categories-dropdown .dropdown-content");
  const shopTrigger = document.getElementById("shop-trigger");
  // --- Carga de categorías ---
  async function loadCategories() {
    const categories = await API.getCategories();
    renderCategories(categories);
  }

  function renderCategories(categorias) {
    if (!Array.isArray(categorias)) return;
    categoriesDropdown.innerHTML = "";

    categorias.forEach(cat => {
      const link = document.createElement("a");
      link.href = "#";
      link.className = "dropdown-category";
      link.textContent = cat.nombre;
      link.dataset.categoryId = cat.id;

      // 🔑 Redirección al hacer click
      link.addEventListener("click", (e) => {
        e.preventDefault();
        window.location.href = `/categoria.html?id=${cat.id}`;
      });

      categoriesDropdown.appendChild(link);
    });
  }

  function initializeDropdown() {
    if (!shopTrigger) return;
    const categoriesDropdownMenu = document.getElementById("categories-dropdown");

    shopTrigger.addEventListener("mouseenter", () => {
      categoriesDropdownMenu.classList.add("show");
    });

    const navDropdown = shopTrigger.parentElement;
    navDropdown.addEventListener("mouseleave", () => {
      categoriesDropdownMenu.classList.remove("show");
    });
  }

  loadCategories();
  initializeDropdown();

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
    await this.initializeMercadoPago();
    // En algún lugar de tu código
    window.MERCADOPAGO_PUBLIC_KEY = "APP_USR-c9d6ba4f-ff10-4d26-b362-fb393755c1b7";
    // Initialize navigation components
    initializeDropdown();
    initializeMobileMenu();
    loadCategories();
});
