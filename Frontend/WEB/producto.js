// Product page functionality
let currentProduct = null;
let selectedColor = null;
let selectedWiring = 'cordless';
let currentRating = 0;
const categoriesDropdown = document.getElementById('categories-dropdown');

// Get product ID from URL
function getProductIdFromUrl() {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get('id');
}
// Load product data
async function loadProduct() {
    const productId = getProductIdFromUrl();
    if (!productId) {
        window.location.href = 'index.html';
        return;
    }

    try {
        const product = await API.getCompleteProductById(productId);
        const categories = await API.getCategories(); // 1. Trae categorías

        if (product && product.producto) {
            // 2. Busca la categoría correspondiente
            const categoriaEncontrada = categories.find(c => c.id === product.producto.categoriaId);

            currentProduct = {
                ...product.producto,
                imagenes: product.archivos?.map(a => ({
                    url: a.linkArchivo,
                    descripcion: `Imagen ${a.orden}`
                })) || [],
                categoria: {
                    id: product.producto.categoriaId,
                    nombre: categoriaEncontrada?.nombre || "Categoría genérica" // 3. Usa el nombre si existe
                }
            };

            displayProduct(currentProduct);
            loadReviews(productId);
        } else {
            showProductNotFound();
        }
    } catch (error) {
        console.error('Error loading product:', error);
        showProductNotFound();
    }
}




// Display product information
function displayProduct(product) {
    // Update breadcrumb
    document.getElementById('breadcrumb-category').textContent = product.categoria.nombre;
    const categoriaSlug = product.categoria.nombre
    .toLowerCase()
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/\s+/g, '-');

    document.getElementById('breadcrumb-category').href = `categoria.html?categoria=${encodeURIComponent(categoriaSlug)}`;
    document.getElementById('breadcrumb-product').textContent = product.nombre;

    // Update product info
    document.getElementById('product-title').textContent = product.nombre;
    document.getElementById('product-price').textContent = formatPrice(product.precio);
    document.getElementById('product-description').textContent = product.descripcion || 'Sin descripción disponible';

    // Display images
    displayProductImages(product.imagenes || []);

    // Generate mock product details
    generateProductDetails(product);

    // Initialize color options (mock colors)
    initializeColorOptions();

    // Initialize wiring options
    initializeWiringOptions();

    // Update rating display
    updateRatingDisplay(4.5, 45); // Mock rating
}

// Display product images
function displayProductImages(images) {
    const mainImage = document.getElementById('main-product-image');
    const thumbnailsContainer = document.getElementById('image-thumbnails');

    if (images.length === 0) {
        mainImage.src = 'https://via.placeholder.com/600x600?text=Sin+Imagen';
        mainImage.alt = 'Sin imagen disponible';
        mainImage.draggable = false;
        mainImage.style.userSelect = 'none';
        mainImage.addEventListener('contextmenu', e => e.preventDefault());
        thumbnailsContainer.innerHTML = '';
        return;
    }

    // Mostrar la primera imagen como principal
    const [firstImage, ...otherImages] = images;
    mainImage.src = firstImage.url;
    mainImage.alt = firstImage.descripcion || 'Producto';
    mainImage.draggable = false;
    mainImage.style.userSelect = 'none';
    mainImage.addEventListener('contextmenu', e => e.preventDefault());

    // Miniaturas solo de las otras imágenes
    thumbnailsContainer.innerHTML = '';
    otherImages.forEach((image) => {
        const thumbnail = document.createElement('div');
        thumbnail.className = 'thumbnail';

        const img = document.createElement('img');
        img.src = image.url;
        img.alt = image.descripcion || 'Imagen del producto';
        img.draggable = false;
        img.style.userSelect = 'none';
        img.addEventListener('contextmenu', e => e.preventDefault());

        thumbnail.appendChild(img);

        thumbnailsContainer.appendChild(thumbnail);
    });
}



// Generate mock product details
function generateProductDetails(product) {
    const alto = product.dimensionAlto || 'N/A';
    const ancho = product.dimensionAncho || 'N/A';
    const profundidad = product.dimensionProfundidad || 'N/A';

    const dimensiones = `${alto} x ${ancho} x ${profundidad}`;

    document.getElementById('product-dimensions').textContent = dimensiones;
    document.getElementById('product-material').textContent = product.material || 'N/A';
    document.getElementById('product-weight').textContent = product.peso ? `${product.peso} kg` : 'N/A';
    document.getElementById('product-tecnica').textContent = product.tecnica || 'N/A'; // si querés mostrar "técnica"
}


// Initialize color options
function initializeColorOptions() {
    const colorContainer = document.getElementById('color-options');
    const colors = [
        { name: 'Natural', value: '#F5F5DC' },
        { name: 'Mint', value: '#98FB98' },
        { name: 'Pink', value: '#FFB6C1' },
        { name: 'Peach', value: '#FFCBA4' }
    ];

    colorContainer.innerHTML = '';
    colors.forEach((color, index) => {
        const colorOption = document.createElement('div');
        colorOption.className = `color-option ${index === 0 ? 'active' : ''}`;
        colorOption.style.backgroundColor = color.value;
        colorOption.title = color.name;
        
        if (index === 0) selectedColor = color.name;
        
        colorOption.addEventListener('click', () => {
            document.querySelectorAll('.color-option').forEach(c => c.classList.remove('active'));
            colorOption.classList.add('active');
            selectedColor = color.name;
        });
        
        colorContainer.appendChild(colorOption);
    });
}

// Initialize wiring options
function initializeWiringOptions() {
    const wiringButtons = document.querySelectorAll('.option-btn[data-wiring]');
    
    wiringButtons.forEach(button => {
        button.addEventListener('click', () => {
            wiringButtons.forEach(b => b.classList.remove('active'));
            button.classList.add('active');
            selectedWiring = button.dataset.wiring;
        });
    });
}

// Update rating display
function updateRatingDisplay(rating, reviewCount) {
    const starsContainer = document.getElementById('product-stars');
    const reviewCountElement = document.getElementById('review-count');
    
    // Generate stars
    starsContainer.innerHTML = generateStars(rating);
    reviewCountElement.textContent = `${reviewCount} reviews`;
    
    // Update large rating display
    document.getElementById('big-rating').textContent = rating.toFixed(1);
    document.getElementById('stars-large').innerHTML = generateStars(rating, 'large');
    document.getElementById('total-reviews').textContent = `Basado en ${reviewCount} reseñas`;
    
    // Generate rating breakdown
    generateRatingBreakdown(reviewCount);
}

// Generate stars HTML
function generateStars(rating, size = 'normal') {
    const fullStars = Math.floor(rating);
    const hasHalfStar = rating % 1 >= 0.5;
    const emptyStars = 5 - fullStars - (hasHalfStar ? 1 : 0);
    
    let starsHTML = '';
    
    // Full stars
    for (let i = 0; i < fullStars; i++) {
        starsHTML += '★';
    }
    
    // Half star
    if (hasHalfStar) {
        starsHTML += '☆';
    }
    
    // Empty stars
    for (let i = 0; i < emptyStars; i++) {
        starsHTML += '☆';
    }
    
    return starsHTML;
}

// Generate rating breakdown
function generateRatingBreakdown(totalReviews) {
    const breakdownContainer = document.getElementById('rating-breakdown');
    const ratings = [
        { stars: 5, count: Math.floor(totalReviews * 0.6) },
        { stars: 4, count: Math.floor(totalReviews * 0.25) },
        { stars: 3, count: Math.floor(totalReviews * 0.1) },
        { stars: 2, count: Math.floor(totalReviews * 0.03) },
        { stars: 1, count: Math.floor(totalReviews * 0.02) }
    ];
    
    breakdownContainer.innerHTML = '';
    ratings.forEach(rating => {
        const percentage = totalReviews > 0 ? (rating.count / totalReviews) * 100 : 0;
        
        const ratingBar = document.createElement('div');
        ratingBar.className = 'rating-bar';
        ratingBar.innerHTML = `
            <span>${rating.stars} ★</span>
            <div class="rating-bar-fill">
                <div class="rating-bar-progress" style="width: ${percentage}%"></div>
            </div>
            <span>${rating.count}</span>
        `;
        
        breakdownContainer.appendChild(ratingBar);
    });
}

// Load reviews
async function loadReviews(productId) {
    try {
        // Mock reviews data (in a real app, this would come from the backend)
        const mockReviews = [
            {
                id: 1,
                name: 'María García',
                rating: 5,
                title: 'Excelente producto',
                text: 'Me encanta esta lámpara. La calidad es excelente y la luz es muy cálida.',
                date: '2024-01-15'
            },
            {
                id: 2,
                name: 'Carlos Ruiz',
                rating: 4,
                title: 'Muy buena compra',
                text: 'Producto de buena calidad, llegó rápido y bien empacado.',
                date: '2024-01-10'
            },
            {
                id: 3,
                name: 'Ana Martínez',
                rating: 5,
                title: 'Recomendado',
                text: 'Hermosa lámpara, perfecta para mi mesa de noche. Muy satisfecha.',
                date: '2024-01-05'
            }
        ];
        
        displayReviews(mockReviews);
    } catch (error) {
        console.error('Error loading reviews:', error);
    }
}

// Display reviews
function displayReviews(reviews) {
    const reviewsList = document.getElementById('reviews-list');
    
    if (reviews.length === 0) {
        reviewsList.innerHTML = '<p>No hay reseñas aún. ¡Sé el primero en escribir una!</p>';
        return;
    }
    
    reviewsList.innerHTML = '';
    reviews.forEach(review => {
        const reviewElement = document.createElement('div');
        reviewElement.className = 'review-item';
        reviewElement.innerHTML = `
            <div class="review-header">
                <div class="reviewer-info">
                    <h4>${review.name}</h4>
                    <div class="review-rating">${generateStars(review.rating)}</div>
                </div>
                <div class="review-date">${formatDate(review.date)}</div>
            </div>
            <h5 class="review-title">${review.title}</h5>
            <p class="review-text">${review.text}</p>
        `;
        
        reviewsList.appendChild(reviewElement);
    });
}

// Format price
function formatPrice(price) {
    return new Intl.NumberFormat('es-CL', {
        style: 'currency',
        currency: 'CLP'
    }).format(price);
}

// Format date
function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('es-ES', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    });
}

// Show product not found
function showProductNotFound() {
    document.querySelector('.product-page').innerHTML = `
        <div class="container">
            <div style="text-align: center; padding: 4rem 0;">
                <h1>Producto no encontrado</h1>
                <p>El producto que buscas no existe o ha sido eliminado.</p>
                <a href="index.html" class="btn-primary">Volver al inicio</a>
            </div>
        </div>
    `;
}

// Modal functionality
function initializeModal() {
    const modal = document.getElementById('review-modal');
    const writeReviewBtn = document.getElementById('write-review-btn');
    const closeModalBtn = document.getElementById('close-review-modal');
    const reviewForm = document.getElementById('review-form');
    const starRating = document.getElementById('star-rating');
    
    // Open modal
    writeReviewBtn.addEventListener('click', () => {
        modal.classList.add('active');
        document.body.style.overflow = 'hidden';
    });
    
    // Close modal
    closeModalBtn.addEventListener('click', closeModal);
    modal.addEventListener('click', (e) => {
        if (e.target === modal) closeModal();
    });
    
    function closeModal() {
        modal.classList.remove('active');
        document.body.style.overflow = 'auto';
        reviewForm.reset();
        currentRating = 0;
        updateStarRating();
    }
    
    // Star rating functionality
    const stars = starRating.querySelectorAll('.star');
    stars.forEach((star, index) => {
        star.addEventListener('click', () => {
            currentRating = index + 1;
            updateStarRating();
        });
        
        star.addEventListener('mouseenter', () => {
            updateStarRating(index + 1);
        });
    });
    
    starRating.addEventListener('mouseleave', () => {
        updateStarRating();
    });
    
    function updateStarRating(hoverRating = null) {
        const rating = hoverRating || currentRating;
        stars.forEach((star, index) => {
            star.classList.toggle('active', index < rating);
        });
    }
    
    // Form submission
    reviewForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        if (currentRating === 0) {
            alert('Por favor selecciona una calificación');
            return;
        }
        
        const formData = new FormData(reviewForm);
        const reviewData = {
            productId: getProductIdFromUrl(),
            rating: currentRating,
            title: formData.get('title') || document.getElementById('review-title').value,
            text: formData.get('text') || document.getElementById('review-text').value,
            name: formData.get('name') || document.getElementById('reviewer-name').value
        };
        
        try {
            // In a real app, submit to backend
            console.log('Submitting review:', reviewData);
            alert('¡Gracias por tu reseña! Se ha enviado correctamente.');
            closeModal();
            
            // Reload reviews (in a real app)
            // await loadReviews(reviewData.productId);
        } catch (error) {
            console.error('Error submitting review:', error);
            alert('Error al enviar la reseña. Por favor intenta de nuevo.');
        }
    });
}

// Add to cart functionality
function initializeAddToCart() {
    const addToCartBtn = document.getElementById('add-to-cart-btn');
    
    addToCartBtn.addEventListener('click', () => {
        if (!currentProduct) return;
        
        const cartItem = {
            id: currentProduct.id,
            name: currentProduct.nombre,
            price: currentProduct.precio,
            color: selectedColor,
            wiring: selectedWiring,
            image: currentProduct.imagenes?.[0]?.url || 'https://via.placeholder.com/100x100',
            quantity: 1
        };
        
        // Add to cart (in a real app, this would update the cart state)
        console.log('Adding to cart:', cartItem);
        
        // Update cart count (mock)
        const cartCount = document.querySelector('.cart-count');
        if (cartCount) {
            const currentCount = parseInt(cartCount.textContent) || 0;
            cartCount.textContent = currentCount + 1;
        }
        
        // Show success message
        addToCartBtn.textContent = '✓ AGREGADO';
        addToCartBtn.style.background = '#059669';
        
        setTimeout(() => {
            addToCartBtn.textContent = 'AGREGAR AL CARRITO';
            addToCartBtn.style.background = '#059669';
        }, 2000);
    });
}

// Scroll functionality for sticky sidebar
function initializeScrollBehavior() {
    const productInfo = document.getElementById('product-info');
    const productDetails = document.getElementById('product-details');
    
    window.addEventListener('scroll', () => {
        const detailsRect = productDetails.getBoundingClientRect();
        const windowHeight = window.innerHeight;
        
        // If product details are in view, make the sidebar less sticky
        if (detailsRect.top < windowHeight && detailsRect.bottom > 0) {
            productInfo.style.position = 'relative';
            productInfo.style.top = 'auto';
        } else {
            productInfo.style.position = 'sticky';
            productInfo.style.top = '2rem';
        }
    });
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

// Initialize page
document.addEventListener('DOMContentLoaded', () => {
    loadProduct();
    initializeModal();
    initializeAddToCart();
    initializeScrollBehavior();
    initializeDropdown();
    loadCategories();
    document.addEventListener('click', handleClicks);
});