let productsGrid;
let categoriesDropdown;
let heroTitle;
let heroDescription;
let heroBadge;
let heroBg;
// Initialize the application
document.addEventListener('DOMContentLoaded', () => {
    // DOM elements
    productsGrid = document.getElementById('products-grid');
    categoriesDropdown = document.querySelector('#categories-dropdown .dropdown-content');
    heroTitle = document.getElementById('hero-title');
    heroDescription = document.getElementById('hero-description');
    heroBadge = document.getElementById('hero-badge');
    heroBg = document.querySelector('.hero-bg');
    loadCategories();
    loadProducts();
    loadLatestProduct();
    initializeEventListeners();
    const mobileButton = document.querySelector('.mobile-menu'); // encuentra ese botón nuevo
    const mobileNav = document.getElementById('mobile-nav');

    mobileButton.addEventListener('click', () => {
        mobileNav.classList.toggle('show');  // muestra/oculta menú móvil
    });


    document.addEventListener('click', (e) => {
        if (!mobileNav.contains(e.target) && !mobileButton.contains(e.target)) {
            mobileNav.classList.remove('show');
        }
    });
    document.addEventListener('click', handleClicks); 

});

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


// Load products from API
async function loadProducts() {
    const products = await API.getCompleteProducts(); 
    renderProducts(products);
}

// Load products by category
async function loadProductsByCategory(categoryId) {
    const products = await API.getProductsByCategory(categoryId);
    renderProducts(products);
}

// Render products in grid
function renderProducts(products) {
    productsGrid.innerHTML = '';

    products.forEach(productDTO => {
        const productCard = createProductCard(productDTO);
        productsGrid.appendChild(productCard);
    });
}

// Create product card element
function createProductCard(dto) {
    const { producto, archivoPrincipal } = dto;

    const card = document.createElement('div');
    card.className = 'product-card';
    card.dataset.productId = producto.id;
    card.style.cursor = 'pointer';

    card.innerHTML = `
        <div class="product-image">
            <img src="${archivoPrincipal?.linkArchivo || 'src/assets/product-grid.jpg'}" alt="${producto.nombre}">
        </div>
        <div class="product-info">
            <h3 class="product-name">${producto.nombre}</h3>
            <p class="product-price">$${producto.precio}</p>
        </div>
    `;

    return card;
}

// Load latest product for hero section
async function loadLatestProduct() {
    const latestProductDTO = await API.getLatestProduct();
    updateHeroSection(latestProductDTO);
}

// Update hero section con datos adaptados
function updateHeroSection(productDTO) {
    if (productDTO && productDTO.producto) {
        heroTitle.textContent = productDTO.producto.nombre;
        
        // Si tienes descripción en ProductoDTO
        heroDescription.textContent = productDTO.producto.descripcion || '';
        
        // Badge NEW si tienes lógica para determinar si es nuevo o no,
        // sino puedes usar isNew que pusiste en fallback (modificar según necesidad)
        heroBadge.textContent = productDTO.isNew ? 'NUEVO' : 'NUEVO';
        
        // Actualizar imagen con archivoPrincipal.linkArchivo
        if (productDTO.archivoPrincipal && productDTO.archivoPrincipal.linkArchivo) {
            heroBg.style.backgroundImage = `url(${productDTO.archivoPrincipal.linkArchivo})`;
        }
        
        // Guardar ID para navegación (botón)
        const heroBtn = document.querySelector('.hero-btn');
        if (heroBtn) {
            heroBtn.dataset.productId = productDTO.producto.id;
        }
    }
}

// Add to cart functionality (placeholder)
function addToCart(productId) {
    console.log(`Adding product ${productId} to cart`);
    
    // Update cart count
    const cartCount = document.querySelector('.cart-count');
    const currentCount = parseInt(cartCount.textContent);
    cartCount.textContent = currentCount + 1;
}

// Initialize all event listeners
function initializeEventListeners() {
    // Main click event handler
    document.addEventListener('click', handleClicks);
    
    // Shop dropdown functionality
    initializeDropdown();
    
}

// Handle all click events
function handleClicks(e) {
    // Handle hero button click - navigate to product
    if (e.target.classList.contains('hero-btn')) {
        const productId = e.target.dataset.productId;
        if (productId) {
            window.location.href = `producto.html?id=${productId}`;
        } else {
            document.querySelector('.bestsellers').scrollIntoView({ 
                behavior: 'smooth' 
            });
        }
    }
    
    // Handle product card clicks - navigate to product page
    if (e.target.closest('.product-card')) {
        const productCard = e.target.closest('.product-card');
        const productId = productCard.dataset.productId;
        if (productId) {
            window.location.href = `/producto.html?id=${productId}`;
        }
    }
    
    // Handle view all button - navigate to products page
    if (e.target.classList.contains('view-all-btn')) {
        window.location.href = '/products';
    }
    
    // Handle explore all products button - navigate to products page
    if (e.target.classList.contains('featured-btn')) {
        window.location.href = `/categoria.html?categoria=all`;
    }
    
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

// Utility function to format price
function formatPrice(price) {
    return new Intl.NumberFormat('es-CL', {
        style: 'currency',
        currency: 'CLP'
    }).format(price);
}
