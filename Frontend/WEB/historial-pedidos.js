// Datos simulados de pedidos para desarrollo
const sampleOrders = [
    {
        id: "PED-001",
        date: "2024-01-15",
        status: "PAGADO",
        products: [
            {
                id: 1,
                name: "Farolito Lamp",
                color: "Rojo",
                quantity: 2,
                price: 12500,
                esDigital: 0,
                image: "product1.jpg"
            },
            {
                id: 2,
                name: "Mesa de Centro",
                color: "Madera Natural",
                quantity: 1,
                price: 35000,
                esDigital: 0,
                image: "product2.jpg"
            }
        ]
    },
    {
        id: "PED-002",
        date: "2024-01-20",
        status: "EN PROCESO",
        products: [
            {
                id: 3,
                name: "Modelo Digital 3D - Silla",
                quantity: 1,
                price: 5000,
                esDigital: 1,
                image: "product3.jpg"
            }
        ]
    },
    {
        id: "PED-003", 
        date: "2024-01-25",
        status: "FALLIDO",
        products: [
            {
                id: 4,
                name: "Lampara de Escritorio",
                color: "Negro",
                quantity: 1,
                price: 8500,
                esDigital: 0,
                image: "product4.jpg"
            }
        ]
    },
    {
        id: "PED-004",
        date: "2023-12-10", // Más de 30 días - no debe mostrar reembolso
        status: "PAGADO",
        products: [
            {
                id: 5,
                name: "Estantería Modular",
                color: "Blanco",
                quantity: 1,
                price: 25000,
                esDigital: 0,
                image: "product5.jpg"
            }
        ]
    }
];

// Estado de la aplicación
let orders = [];
let isLoading = true;

// Elementos del DOM
const loadingState = document.getElementById('loading-state');
const emptyOrders = document.getElementById('empty-orders');
const ordersList = document.getElementById('orders-list');

// Función para simular carga de datos del backend
async function loadOrders() {
    try {
        isLoading = true;
        updateUI();

        // 👇 Sacar el userId guardado en localStorage
        const userId = localStorage.getItem("usuarioId");
        if (!userId) {
            throw new Error("No se encontró usuarioId en localStorage");
        }

        // 🔥 Llamada real al backend
        orders = await API.verPedidosDeUsuario(userId);
        console.log("Pedidos recibidos:", orders); // 👈 LOG

    } catch (error) {
        console.error("Error loading orders:", error);
        orders = [];
    } finally {
        isLoading = false;
        updateUI();
    }
}


// Función para actualizar la UI
function updateUI() {
    if (isLoading) {
        loadingState.style.display = 'flex';
        emptyOrders.style.display = 'none';
        ordersList.style.display = 'none';
        return;
    }
    
    loadingState.style.display = 'none';
    
    if (orders.length === 0) {
        emptyOrders.style.display = 'flex';
        ordersList.style.display = 'none';
    } else {
        emptyOrders.style.display = 'none';
        ordersList.style.display = 'block';
        renderOrders();
    }
}

// Función para renderizar los pedidos
function renderOrders() {
    ordersList.innerHTML = orders.map(order => createOrderHTML(order)).join('');
    
    // Agregar event listeners a los botones
    attachEventListeners();
}
function createOrderHTML(order) {
    const orderDate = new Date(order.fechaPedido).toLocaleDateString('es-ES', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    });

    // 👇 si productos viene null, lo cambiamos a []
    const productos = order.productos || [];

    const totalPrice = productos.reduce((sum, product) => sum + (product.precio * product.cantidad), 0);

    const statusClass = getStatusClass(order.estado);
    const statusText = order.estado;

    return `
        <div class="order-card">
            <div class="order-header">
                <div class="order-info">
                    <div class="order-id">Pedido #${order.id}</div>
                    <div class="order-date">${orderDate}</div>
                </div>
                <div class="order-status ${statusClass}">
                    ${statusText}
                </div>
            </div>
            
            <div class="order-products">
                ${productos.map(product => createProductHTML(product)).join('')}
            </div>
            
            <div class="order-footer">
                <div class="order-total">
                    Total: $${order.total.toLocaleString('es-ES')}
                </div>
                <div class="order-actions">
                    ${createActionButtons(order)}
                </div>
            </div>
        </div>
    `;
}
function createProductHTML(product) {
    const colorHTML = !product.esDigital && product.colorNombre ? 
        `<div class="product-color">
            <span>Color:</span>
            <div class="color-indicator" style="background-color: ${getColorHex(product.colorNombre)}"></div>
            <span>${product.colorNombre}</span>
        </div>` : '';

    const precioFinal = product.precioTotal ?? (product.precio * product.cantidad);

    const imageHTML = product.imagen
        ? `<img src="${product.imagen}" alt="${product.nombre ?? 'Producto'}" />`
        : `<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
                <circle cx="9" cy="9" r="2"/>
                <path d="m21 15-3.086-3.086a2 2 0 0 0-2.828 0L6 21"/>
           </svg>`;

    // Botón de descarga para productos digitales
    const downloadButton = product.esDigital && product.archivoBase64
        ? `<a href="data:application/zip;base64,${product.archivoBase64}" 
            download="${product.nombre || 'archivo'}.zip" 
            class="download-btn">
            Descargar ZIP
        </a>`
        : '';


    return `
        <div class="product-item">
            <div class="product-image">
                ${imageHTML}
            </div>
            <div class="product-details">
                <div class="product-name">${product.nombre ?? product.nombreProducto}</div>
                <div class="product-meta">
                    ${colorHTML}
                    <div class="product-quantity">Cantidad: ${product.cantidad}</div>
                </div>
                ${downloadButton}
            </div>
            <div class="product-price">$${precioFinal.toLocaleString('es-ES')}</div>
        </div>
    `;
}

// Función para crear botones de acción
function createActionButtons(order) {
    let buttons = '';
    
    // Botón "Reintentar pago" para estados EN PROCESO y FALLIDO
    if (order.estado === 'PROCESANDO' || order.estado === 'FALLIDO') {
        buttons += `<button class="retry-payment-btn" data-order-id="${order.id}">Reintentar pago</button>`;
    }
    return buttons;
}


// Función para obtener la clase CSS del estado
function getStatusClass(status) {
    switch (status) {
        case 'PAGADO':
            return 'status-paid';
        case 'EN PROCESO':
            return 'status-processing';
        case 'FALLIDO':
            return 'status-failed';
        default:
            return 'status-processing';
    }
}

// Función para obtener color hexadecimal basado en el nombre del color
function getColorHex(colorName) {
    const colors = {
        'Rojo': '#ef4444',
        'Azul': '#3b82f6',
        'Verde': '#10b981',
        'Amarillo': '#f59e0b',
        'Negro': '#1f2937',
        'Blanco': '#f9fafb',
        'Madera Natural': '#d2691e',
        'Gris': '#6b7280'
    };
    
    return colors[colorName] || '#6b7280';
}
async function handleRetryPayment(orderId) {
    console.log('Redirigiendo a reintento de pago para pedido:', orderId);

    // Buscar el pedido en el array global orders
    const order = orders.find(p => p.id == orderId);
    if (!order) {
        alert("No se encontró el pedido");
        return;
    }

    try {
        // Calcular la cantidad total de productos en el pedido
        const totalQuantity = order.productos.reduce((sum, p) => sum + p.cantidad, 0);

        // Llamar a la función global API
        const initPoint = await API.confirmarPedido(order, totalQuantity);

        if (initPoint) {
            // Abrir MercadoPago en nueva ventana
            window.open(initPoint, "_blank");
        } else {
            alert("No se pudo generar el link de pago");
        }
    } catch (err) {
        console.error("Error reintentando pago:", err);
        alert("Ocurrió un error al intentar reintentar el pago.");
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
// Función para agregar event listeners
function attachEventListeners() {
    // Event listeners para botones de reintentar pago
    document.querySelectorAll('.retry-payment-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const orderId = e.target.getAttribute('data-order-id');
            handleRetryPayment(orderId);
        });
    });
}

// Inicializar la página cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', function() {
    console.log('Historial de pedidos cargado');
    loadOrders();
});

// Función para actualizar el contador del carrito (si existe)
if (typeof updateCartCount === 'function') {
    updateCartCount();
}