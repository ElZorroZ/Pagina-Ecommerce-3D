// Product page functionality
let currentProduct = null;
let selectedColor = null;
let selectedWiring = 'cordless';
let selectedFormat = 'fisico';
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
                    nombre: categoriaEncontrada?.nombre || "Categoría genérica"
                },
                colores: product.colores?.map(c => ({
                    id: c.id,                     // ← agregamos el colorId
                    nombre: c.nombre || 'Color desconocido',
                    hex: c.hex || '#cccccc'
                })) || []

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

    document.getElementById('breadcrumb-category').href = `/categoria.html?categoria=${encodeURIComponent(categoriaSlug)}`;
    document.getElementById('breadcrumb-product').textContent = product.nombre;

    // Update product info
    document.getElementById('product-title').textContent = product.nombre;
    updateProductPrice(product);
    document.getElementById('product-description').textContent = product.descripcion || 'Sin descripción disponible';

    // Display images
    displayProductImages(product.imagenes || []);

    // Generate mock product details
    generateProductDetails(product);

    // Initialize format options
    initializeFormatOptions();

    // Initialize color options with real product colors
    initializeColorOptions(product);

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


// Update product price based on selected format
function updateProductPrice(product) {
    const basePrice = product.precio || 0;
    const digitalPrice = product.precioDigital; 
    
    const currentPrice = selectedFormat === 'digital' ? digitalPrice : basePrice;
    document.getElementById('product-price').textContent = formatPrice(currentPrice);
}

// Initialize format options
function initializeFormatOptions() {
    const formatButtons = document.querySelectorAll('.format-option');

    // Forzar formato físico desde el inicio
    selectedFormat = 'fisico';

    formatButtons.forEach(button => {
        // Quitar listener si querés que no se pueda cambiar
        button.classList.remove('active');
        if (button.dataset.format === 'fisico') button.classList.add('active');
        
        button.addEventListener('click', () => {
            // Siempre forzar físico
            selectedFormat = 'fisico';

            formatButtons.forEach(b => b.classList.remove('active'));
            button.classList.add('active');

            // Actualizar precio (si aplica)
            if (currentProduct) {
                updateProductPrice(currentProduct);
            }

            // Mostrar siempre selector de color
            const colorSelector = document.getElementById('color-selector');
            if (colorSelector) colorSelector.style.display = 'block';
        });
    });
}

let selectedColorId = null; // variable global o dentro del scope

function initializeColorOptions(product) {
    const colorContainer = document.getElementById('color-options');
    const colors = product.colores || [];

    colorContainer.innerHTML = '';
    if (colors.length === 0) return;

    colors.forEach((color, index) => {
        const colorOption = document.createElement('div');
        colorOption.className = `color-option ${index === 0 ? 'active' : ''}`;
        colorOption.style.backgroundColor = color.hex || '#cccccc';
        colorOption.title = color.nombre || 'Color desconocido';

        // Seleccionamos el primer color por defecto
        if (index === 0) {
            selectedColor = color.nombre;
            selectedColorId = color.id; // <--- guardar el id
        }

        colorOption.addEventListener('click', () => {
            document.querySelectorAll('.color-option').forEach(c => c.classList.remove('active'));
            colorOption.classList.add('active');
            selectedColor = color.nombre;
            selectedColorId = color.id; // <--- actualizar id
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
function getCurrentUserRole() {
    const token = localStorage.getItem('accessToken'); // tu token se llama accessToken
    if (!token) return null;

    const payload = JSON.parse(atob(token.split('.')[1]));
    // Tomamos el primer rol del arreglo roles
    return Array.isArray(payload.roles) && payload.roles.length > 0 ? payload.roles[0] : null;
}
function formatDate(dateInput) {
    if (!dateInput) return '';
    
    // Tomar solo la parte de la fecha (antes de 'T' si existe)
    const dateOnly = dateInput.split('T')[0];
    const [year, month, day] = dateOnly.split('-');
    
    const resultado = `${day}/${month}/${year}`;
    return resultado;
}
// Generar estrellas HTML
function generateStars(rating, size = 'normal') {
    const fullStars = Math.floor(rating);
    const hasHalfStar = rating % 1 >= 0.5;
    const emptyStars = 5 - fullStars - (hasHalfStar ? 1 : 0);

    let starsHTML = '';

    for (let i = 0; i < fullStars; i++) starsHTML += '★';
    if (hasHalfStar) starsHTML += '☆';
    for (let i = 0; i < emptyStars; i++) starsHTML += '☆';

    return `<span class="${size}-star">${starsHTML}</span>`;
}

// Generar breakdown de calificaciones - SOLO REVIEWS PRINCIPALES
function generateRatingBreakdown(mainReviews) {
    const breakdownContainer = document.getElementById('rating-breakdown');
    if (!Array.isArray(mainReviews)) mainReviews = [];

    const totalReviews = mainReviews.length;
    const ratings = [5, 4, 3, 2, 1].map(stars => ({
        stars,
        count: mainReviews.filter(r => r.calificacion === stars).length
    }));

    breakdownContainer.innerHTML = '';
    ratings.forEach(r => {
        const percentage = totalReviews > 0 ? (r.count / totalReviews) * 100 : 0;
        const ratingBar = document.createElement('div');
        ratingBar.className = 'rating-bar';
        ratingBar.innerHTML = `
            <span>${r.stars} ★</span>
            <div class="rating-bar-fill">
                <div class="rating-bar-progress" style="width: ${percentage}%"></div>
            </div>
            <span>${r.count}</span>
        `;
        breakdownContainer.appendChild(ratingBar);
    });

    return totalReviews;
}

// Actualizar display de calificación promedio - CORREGIDO
function updateRatingDisplay(avgRating, mainReviews) {
    const totalReviews = mainReviews.length;
    document.getElementById('product-stars').innerHTML = generateStars(avgRating);
    document.getElementById('review-count').textContent = `${totalReviews} reviews`;
    document.getElementById('big-rating').textContent = avgRating.toFixed(1);
    document.getElementById('stars-large').innerHTML = generateStars(avgRating, 'large');
    document.getElementById('total-reviews').textContent = totalReviews > 0 
        ? `Basado en ${totalReviews} reseñas`
        : 'Sin reseñas aún';

    generateRatingBreakdown(mainReviews);
}

// Cargar reviews - CORREGIDO PARA MOSTRAR SOLO REVIEWS PRINCIPALES
async function loadReviews(productId) {
    try {
        const allData = await API.obtenerReviews(productId); // Array que puede incluir reviews y respuestas mezcladas
        const currentUserRole = getCurrentUserRole();
        console.log(currentUserRole)
        console.log('Datos completos desde API:', allData);

        // CORRECCIÓN: Filtrar solo las reviews principales
        // Las respuestas tienen calificacion: 0, las reviews principales tienen calificacion: 1-5
        const mainReviews = Array.isArray(allData) 
            ? allData.filter(r => r.calificacion > 0)
            : [];

        console.log('Reviews principales filtradas (calificacion > 0):', mainReviews);

        // Mostrar solo las reviews principales
        displayReviews(mainReviews, currentUserRole);

        // Calcular promedio solo de las reviews principales
        const avgRating = mainReviews.length > 0
            ? mainReviews.reduce((sum, r) => sum + r.calificacion, 0) / mainReviews.length
            : 0;

        console.log('Promedio calculado:', avgRating, 'de', mainReviews.length, 'reviews principales');

        // Pasar el promedio y las reviews principales
        updateRatingDisplay(avgRating, mainReviews);
        
    } catch (error) {
        console.error('Error loading reviews:', error);
        document.getElementById('reviews-list').innerHTML = '<p>No se pudieron cargar las reseñas.</p>';
        updateRatingDisplay(0, []); // Pasar valores seguros
    }
}

function displayReviews(reviews, currentUserRole, purchasedProducts = [], currentProductId) {
    const reviewsList = document.getElementById('reviews-list');
    const writeReviewBtn = document.getElementById('write-review-btn');

    // Normalizar rol
    const role = Array.isArray(currentUserRole) ? currentUserRole[0] : currentUserRole;
    console.log("ROL ACTUAL:", role);

    // Convertimos currentProductId a número
    const productIdNumber = Number(currentProductId);
    const purchasedProductNumbers = purchasedProducts.map(p => Number(p));

    // Mostrar botón "Escribir reseña" solo para clientes que compraron
    writeReviewBtn.style.display = 'none';
    if (role === 'ROLE_CLIENTE' && purchasedProductNumbers.includes(productIdNumber)) {
        writeReviewBtn.style.display = 'inline-block';
    }

    if (!Array.isArray(reviews) || reviews.length === 0) {
        reviewsList.innerHTML = '<p>No hay reseñas aún. ¡Sé el primero en escribir una!</p>';
        return;
    }

    reviewsList.innerHTML = '';

    const mainReviews = reviews.filter(r => r.calificacion != null && r.calificacion > 0);

    mainReviews.forEach(review => {
        const hasRespuesta = review.respuesta && review.respuesta.mensaje;

        const reviewerName = review.nombre && review.apellido
            ? `${review.nombre} ${review.apellido}`
            : `Usuario #${review.usuarioId}`;

        const reviewElement = document.createElement('div');
        reviewElement.className = 'review-item';
        reviewElement.style.position = 'relative';

        // Header
        const header = document.createElement('div');
        header.className = 'review-header';
        const reviewerInfo = document.createElement('div');
        reviewerInfo.className = 'reviewer-info';
        const h4 = document.createElement('h4');
        h4.textContent = reviewerName;
        const ratingDiv = document.createElement('div');
        ratingDiv.className = 'review-rating';
        ratingDiv.innerHTML = generateStars(review.calificacion);
        reviewerInfo.appendChild(h4);
        reviewerInfo.appendChild(ratingDiv);
        header.appendChild(reviewerInfo);
        reviewElement.appendChild(header);

        // Texto review
        const reviewText = document.createElement('p');
        reviewText.className = 'review-text';
        reviewText.textContent = review.mensaje || 'Sin comentario';
        reviewElement.appendChild(reviewText);

        // Respuesta si existe
        if (hasRespuesta) {
            const respName = review.respuesta.nombre && review.respuesta.apellido
                ? `${review.respuesta.nombre} ${review.respuesta.apellido}`
                : `Usuario #${review.respuesta.usuarioId}`;

            const respuestaElement = document.createElement('div');
            respuestaElement.className = 'review-response';
            respuestaElement.innerHTML = `
                <div class="response-header"><strong>${respName} respondió:</strong></div>
                <p>${review.respuesta.mensaje}</p>
            `;
            const connector = document.createElement('div');
            connector.className = 'review-connector';
            const horizontalLine = document.createElement('div');
            horizontalLine.className = 'review-horizontal-line';
            respuestaElement.appendChild(connector);
            respuestaElement.appendChild(horizontalLine);
            reviewElement.appendChild(respuestaElement);
        }

        // Botón Responder (admin/colaborador) solo si NO hay respuesta
        if ((role === 'ROLE_ADMIN' || role === 'ROLE_COLABORADOR') && !hasRespuesta) {
            const replyBtn = document.createElement('button');
            replyBtn.textContent = 'Responder';
            replyBtn.className = 'write-review-btn reply-btn';
            replyBtn.onclick = () => openReplyModal(review.id, reviewElement);
            reviewElement.appendChild(replyBtn);
        }

        // Botón Eliminar (solo admin)
        if (role === 'ROLE_ADMIN') {
            const deleteBtn = document.createElement('button');
            deleteBtn.textContent = 'Eliminar';
            deleteBtn.className = 'write-review-btn delete-btn';
            deleteBtn.onclick = () => {
                mostrarConfirmacion("¿Deseas eliminar esta reseña?", (confirm) => {
                    if (confirm) {
                        handleDelete(review.id, reviewElement)
                            .then(() => mostrarExito("Reseña eliminada"))
                            .catch(() => mostrarError("No se pudo eliminar la reseña"));
                    }
                });
            };
            reviewElement.appendChild(deleteBtn);
        }

        reviewsList.appendChild(reviewElement);
    });
}



async function initReviews(currentProductId) {
    try {
        // 1️⃣ Obtener datos del usuario desde authManager
        const currentUserId = authManager.getUserId();
        const currentUserRole = authManager.getUserRole(); // <-- usar JWT / authManager

        let purchasedProducts = [];

        // 2️⃣ Si el usuario es cliente, traemos sus pedidos para verificar si compró este producto
        if (currentUserRole === 'ROLE_CLIENTE' && currentUserId) {
            console.log("🟢 currentUserId:", currentUserId);
            console.log(`${API_BASE_URL}/api/pedido/verPedidosDeUsuario?id=${currentUserId}`);

            const pedidos = await API.verPedidosDeUsuario(currentUserId);
            console.log("📦 Pedidos obtenidos:", pedidos);

            // Filtrar solo pedidos PAGADOS
            const pedidosPagados = pedidos.filter(pedido => pedido.estado === "PAGADO");
            console.log("✅ Pedidos PAGADOS:", pedidosPagados);

            purchasedProducts = pedidosPagados.flatMap(pedido =>
                pedido.productos.map(prod => Number(prod.productoId))
            );
        }

        // 3️⃣ Traer las reviews del producto
        const reviewsData = await API.obtenerReviews(currentProductId);
        console.log("Reviews obtenidas:", reviewsData);

        // 4️⃣ Mostrar reviews
        displayReviews(reviewsData, currentUserRole, purchasedProducts, currentProductId);

    } catch (error) {
        console.error('Error inicializando reseñas:', error);
    }
}


// Responder review (misma estética)
async function handleReply(reviewId, reviewElement) {
    const replyText = prompt('Escribe tu respuesta:');
    if (!replyText) return;

    try {
        console.log('Respondiendo review con ID:', reviewId);

        // Obtener usuarioId desde AuthManager
        const usuarioId = parseInt(authManager.getUserId());
        if (!usuarioId) {
            throw new Error("Usuario no autenticado");
        }

        const respuesta = await window.API.responderReview(reviewId, { 
            mensaje: replyText,
            usuarioId: usuarioId
        });

        // Obtener el nombre completo del usuario que respondió
        const respName = respuesta.nombre && respuesta.apellido
            ? `${respuesta.nombre} ${respuesta.apellido}`
            : `Usuario #${respuesta.usuarioId}`;

        const respuestaElement = document.createElement('div');
        respuestaElement.className = 'review-response';
        respuestaElement.innerHTML = `
            <div class="response-header">
                <strong>${respName} respondió:</strong>
            </div>
            <p>${respuesta.mensaje}</p>
        `;

        // Estilos tipo hilo
        respuestaElement.style.position = 'relative';
        respuestaElement.style.marginLeft = '20px';
        respuestaElement.style.marginTop = '15px';
        respuestaElement.style.paddingLeft = '20px';
        respuestaElement.style.backgroundColor = '#f8fafc';
        respuestaElement.style.borderRadius = '8px';
        respuestaElement.style.padding = '12px 16px';
        
        // Línea vertical y horizontal
        const connector = document.createElement('div');
        connector.style.position = 'absolute';
        connector.style.left = '-10px';
        connector.style.top = '0px';
        connector.style.width = '2px';
        connector.style.height = '100%';
        connector.style.backgroundColor = '#cbd5e1';
        connector.style.borderRadius = '1px';
        
        const horizontalLine = document.createElement('div');
        horizontalLine.style.position = 'absolute';
        horizontalLine.style.left = '-10px';
        horizontalLine.style.top = '20px';
        horizontalLine.style.width = '15px';
        horizontalLine.style.height = '2px';
        horizontalLine.style.backgroundColor = '#cbd5e1';
        horizontalLine.style.borderRadius = '1px';
        
        respuestaElement.appendChild(connector);
        respuestaElement.appendChild(horizontalLine);

        // Remover el botón "Responder"
        const replyBtn = reviewElement.querySelector('button[onclick*="openReplyModal"]');
        if (replyBtn) replyBtn.remove();

        reviewElement.appendChild(respuestaElement);
        alert('✅ Respuesta enviada correctamente');
    } catch (error) {
        console.error('Error al responder review:', error);
        alert('❌ No se pudo enviar la respuesta. Verifica tu rol o inténtalo de nuevo.');
    }
}


function openReplyModal(reviewId, reviewElement) {
    const modal = document.getElementById('reply-modal');
    const textarea = modal.querySelector('textarea');
    const submitBtn = modal.querySelector('button[type="submit"]');

    textarea.value = '';
    modal.classList.add('active');

    // Evitar duplicar eventos
    const newSubmit = submitBtn.cloneNode(true);
    submitBtn.parentNode.replaceChild(newSubmit, submitBtn);

    newSubmit.onclick = async (e) => {
        e.preventDefault();
        const mensaje = textarea.value.trim();
        if (!mensaje) return;

        try {
            console.log('Respondiendo review con ID (modal):', reviewId);

            // Obtener usuarioId desde AuthManager
            const usuarioId = parseInt(authManager.getUserId());
            if (!usuarioId) throw new Error("Usuario no autenticado");

            const respuesta = await window.API.responderReview(reviewId, { 
                mensaje,
                usuarioId
            });

            // Obtener el nombre completo del usuario que respondió
            const respName = respuesta.nombre && respuesta.apellido
                ? `${respuesta.nombre} ${respuesta.apellido}`
                : `Usuario #${respuesta.usuarioId}`;

            const respuestaElement = document.createElement('div');
            respuestaElement.className = 'review-response';
            respuestaElement.innerHTML = `
                <div class="response-header">
                    <strong>${respName} respondió:</strong>
                </div>
                <p>${respuesta.mensaje}</p>
                ${respuesta.fecha ? `<small>${new Date(respuesta.fecha).toLocaleString()}</small>` : ''}
            `;

            // Estilos tipo árbol/hilo
            respuestaElement.style.position = 'relative';
            respuestaElement.style.marginLeft = '20px';
            respuestaElement.style.marginTop = '15px';
            respuestaElement.style.paddingLeft = '20px';
            respuestaElement.style.backgroundColor = '#f8fafc';
            respuestaElement.style.borderRadius = '8px';
            respuestaElement.style.padding = '12px 16px';
            
            // Crear línea vertical y horizontal
            const connector = document.createElement('div');
            connector.style.position = 'absolute';
            connector.style.left = '-10px';
            connector.style.top = '0px';
            connector.style.width = '2px';
            connector.style.height = '100%';
            connector.style.backgroundColor = '#cbd5e1';
            connector.style.borderRadius = '1px';
            
            const horizontalLine = document.createElement('div');
            horizontalLine.style.position = 'absolute';
            horizontalLine.style.left = '-10px';
            horizontalLine.style.top = '20px';
            horizontalLine.style.width = '15px';
            horizontalLine.style.height = '2px';
            horizontalLine.style.backgroundColor = '#cbd5e1';
            horizontalLine.style.borderRadius = '1px';
            
            respuestaElement.appendChild(connector);
            respuestaElement.appendChild(horizontalLine);

            // Remover botón "Responder"
            const replyBtn = reviewElement.querySelector('button[onclick*="openReplyModal"]');
            if (replyBtn) replyBtn.remove();

            reviewElement.appendChild(respuestaElement);
            modal.classList.remove('active');
            
        } catch (err) {
            console.error('Error al responder:', err);
            alert('❌ No se pudo enviar la respuesta. Verifica tu rol o inténtalo de nuevo.');
        }
    };
}

// Función para eliminar review (solo ADMIN)
async function handleDelete(reviewId, reviewElement) {
    try {
        await window.API.eliminarReview(reviewId);
        
        // Eliminar el elemento del DOM con una pequeña animación
        reviewElement.style.transition = 'opacity 0.3s ease';
        reviewElement.style.opacity = '0';
        
        setTimeout(() => {
            reviewElement.remove();
        }, 300);

        // Ya no usamos alert aquí; mostrarExito se llama desde mostrarConfirmacion
    } catch (error) {
        console.error('Error al eliminar review:', error);
        throw error; // Se lanza para que mostrarError lo capture
    }
}



// Format price
function formatPrice(price) {
    return new Intl.NumberFormat('es-CL', {
        style: 'currency',
        currency: 'CLP'
    }).format(price);
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

    let currentRating = 0;

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
    // Form submission
reviewForm.addEventListener('submit', async (e) => {
    e.preventDefault();

    if (!authManager.isAuthenticated()) {
        mostrarError('Debes iniciar sesión para enviar una reseña.');
        authManager.redirectToLogin();
        return;
    }

    if (currentRating === 0) {
        mostrarError('Por favor selecciona una calificación');
        return;
    }

    const reviewData = {
        productId: getProductIdFromUrl(),
        usuarioId: authManager.getUserId(), // usuario del token
        mensaje: document.getElementById('review-text').value.trim(),
        calificacion: currentRating
    };

    try {
        const response = await authManager.fetchWithAuth(`${API_BASE_URL}/api/reviews`, {
            method: "POST",
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(reviewData)
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Error al enviar la reseña');
        }

        const result = await response.json();
        console.log('Review enviada:', result);
        mostrarExito('¡Gracias por tu reseña! Se ha enviado correctamente.');

        // Recargar la página después de mostrar el mensaje
        setTimeout(() => {
            location.reload();
        }, 1000); // espera 1s para que se vea el mensaje

    } catch (error) {
        console.error('Error al enviar la reseña:', error);
        mostrarError('❌ Error al enviar la reseña. Por favor intenta de nuevo.');
    }
});


}
async function initializeAddToCart() {
    const addToCartBtn = document.getElementById('add-to-cart-btn');

    addToCartBtn.addEventListener('click', async () => {
        if (!authManager.isAuthenticated()) {
            window.location.href = '/usuario/login/login.html';
            return;
        }

        if (!currentProduct) return;

        const basePrice = currentProduct.precio || 0;
        const digitalPrice = currentProduct.precioDigital || (basePrice * 0.7);
        const currentPrice = selectedFormat === 'digital' ? digitalPrice : basePrice;

        const usuarioId = Number(authManager.getUserId());

        const carritoRequest = {
            productoId: currentProduct.id,
            usuarioId, // ahora sí es un número válido
            cantidad: 1,
            precioUnitario: currentPrice,
            precioTotal: currentPrice,
            esDigital: selectedFormat === 'digital',
            colorId: selectedColorId || 0
        };

        console.log("DTO que se va a enviar al backend:", carritoRequest);

        try {
            mostrarCarga("Agregando producto al carrito...");
            // Obtenemos el carrito actual usando fetchWithAuth
            const carritoResponse = await authManager.fetchWithAuth(`${API_BASE_URL}/api/carrito/verCarrito/${usuarioId}`);
            if (!carritoResponse.ok) throw new Error("Error al obtener el carrito");
            const carrito = await carritoResponse.json();

            // Validación digital vs físico
            const productoExistenteDigital = carrito.find(item =>
                item.productoId === currentProduct.id &&
                item.esDigital === 1
            );

            if (productoExistenteDigital && carritoRequest.esDigital) {
                mostrarMensajeError("No puedes agregar el mismo producto en formato digital y físico al carrito.");
                return;
            }

            // Validación físico por color
            const productoFisicoExistente = carrito.find(item =>
                item.productoId === currentProduct.id &&
                item.esDigital === 0 &&
                item.colorId === carritoRequest.colorId
            );

            if (productoFisicoExistente && carritoRequest.esDigital === 0) {
                await authManager.fetchWithAuth(`${API_BASE_URL}/api/carrito/sumarCantidad/${productoFisicoExistente.id}/1`, { method: 'PUT' });
            } else {
                // Agregar producto al carrito
                const response = await authManager.fetchWithAuth(`${API_BASE_URL}/api/carrito/agregarProductoaCarrito`, {
                    method: 'POST',
                    body: JSON.stringify(carritoRequest)
                });

                if (!response.ok) {
                    let mensaje = "Error interno del servidor";
                    try {
                        const errorData = await response.json();
                        mensaje = errorData.reason || errorData.error || mensaje;
                    } catch (e) {
                        if (response.status === 403) mensaje = "No tienes permiso para realizar esta acción.";
                        else if (response.status === 400) mensaje = "Ya existe este producto en el carrito con el mismo color";
                    }
                    mostrarMensajeError(mensaje);
                    return;
                }
            }

            // Actualizar contador
            const cartCount = document.querySelector('.cart-count');
            if (cartCount) {
                const currentCount = parseInt(cartCount.textContent) || 0;
                cartCount.textContent = currentCount + 1;
            }

            addToCartBtn.textContent = '✓ AGREGADO';
            addToCartBtn.style.background = '#059669';
            setTimeout(() => {
                addToCartBtn.textContent = 'AGREGAR AL CARRITO';
                addToCartBtn.style.background = '#059669';
            }, 2000);

            actualizarCantidadCarrito();

        } catch (error) {
            console.error('Error al agregar al carrito:', error);
            mostrarMensajeError("Ocurrió un error al agregar el producto al carrito.");
        }
        finally {
            // Ocultar overlay al terminar
            ocultarCarga();
        }
        
        function mostrarMensajeError(text) {
            addToCartBtn.textContent = 'ERROR';
            addToCartBtn.style.background = '#dc2626';

            const msgDiv = document.createElement('div');
            msgDiv.textContent = text;
            Object.assign(msgDiv.style, {
                position: 'fixed',
                top: '10px',
                right: '10px',
                padding: '10px 20px',
                background: '#dc2626',
                color: 'white',
                borderRadius: '5px',
                zIndex: 1000,
            });
            document.body.appendChild(msgDiv);

            setTimeout(() => {
                addToCartBtn.textContent = 'AGREGAR AL CARRITO';
                addToCartBtn.style.background = '#059669';
                msgDiv.remove();
            }, 3000);
        }
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
    const currentProductId = getProductIdFromUrl(); // sacamos del URL
    if (!currentProductId) return;
    initReviews(currentProductId);
    loadProduct();
    initializeModal();
    initializeAddToCart();
    initializeScrollBehavior();
    initializeDropdown();
    loadCategories();
    document.addEventListener('click', handleClicks);
    const cartBtn = document.querySelector('.cart-btn');
  if (cartBtn) {
    cartBtn.addEventListener('click', () => {
      window.location.href = '/carrito.html';
    });
  }
});