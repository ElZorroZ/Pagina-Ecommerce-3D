// Category page functionality
class CategoryPage {
    constructor() {
        this.currentCategoryId = null;
        this.currentCategoryName = '';
        this.currentCategoryObj = null;
        this.allProducts = [];
        this.filteredProducts = [];
        this.filters = {
            category: 'all',
            price: 'all',
            status: 'all'
        };
        this.sortBy = 'name';
        
    }
    
   async init() {
        this.currentCategoryName = this.getCategoryFromURL();

        await this.loadCategories();

        if (this.currentCategoryName) {
            const categoria = this.categories.find(cat =>
                this.slugify(cat.nombre) === this.currentCategoryName
            );
            if (categoria) {
                this.filters.category = String(categoria.id);
                this.currentCategoryId = Number(categoria.id);
                this.currentCategoryObj = categoria;
            }
        }

        // Ahora que filters está seteado, renderizá filtros con el radio correcto
        this.renderCategoryFilters(this.categories);

        await this.loadProducts();

        this.applyFilters();

        this.initEventListeners();
        this.updateCategoryInfo();
    }

    slugify(nombre) {
        return nombre.toLowerCase().normalize("NFD").replace(/[\u0300-\u036f]/g, "").replace(/\s+/g, '-');
    }


   getCategoryFromURL() {
        const urlParams = new URLSearchParams(window.location.search);
        const categoryName = urlParams.get('categoria');
        return categoryName || null;
    }




    async loadCategories() {
        try {
            const categories = await API.getCategories();
            this.categories = categories; 
            this.renderCategoryFilters(categories);
            this.renderNavigationCategories(categories);

            // Si hay nombre de categoría en la URL, obtener su ID y setear filtros
            if (this.currentCategoryName && !this.currentCategoryId) {
                const matchingCategory = categories.find(cat =>
                    this.slugify(cat.nombre) === this.currentCategoryName
                );
                if (matchingCategory) {
                    this.currentCategoryId = Number(matchingCategory.id);
                    this.filters.category = String(this.currentCategoryId);
                }
            }
        } catch (error) {
            console.error('Error loading categories:', error);
        }
    }

    async loadProducts() {
        try {
            this.showLoading();
            
            let productsResponse;
            if (this.currentCategoryId) {
                productsResponse = await API.getProductsByCategory(this.currentCategoryId);
            } else {
                productsResponse = await API.getAllProducts();            
            }

            console.log('Productos cargados para categoría:', this.currentCategoryId, productsResponse);

            const products = productsResponse?.content || [];

            // Normalizar productos para tener estructura uniforme
            this.allProducts = products.map(p => this.normalizeProductDTO(p));
            this.filteredProducts = [...this.allProducts];
            this.applyFilters();
            this.hideLoading();
        } catch (error) {
            console.error('Error loading products:', error);
            this.hideLoading();
            this.showEmptyState();
        }
    }


    renderCategoryFilters(categories) {
        const container = document.getElementById('category-filters');
        if (!container) return;

        container.innerHTML = `
        <label class="filter-option">
            <input type="radio" name="category" value="all" ${this.filters.category === 'all' ? 'checked' : ''}>
            <span>Todos</span>
        </label>
        `;

        categories.forEach(cat => {
            if (cat.id === 1) return;
            const checked = this.filters.category === String(cat.id) ? 'checked' : '';
            container.innerHTML += `
            <label class="filter-option">
                <input type="radio" name="category" value="${cat.id}" ${checked}>
                <span>${cat.nombre}</span>
            </label>
            `;
        });
    }



    renderNavigationCategories(categories) {
        const categoriesDropdown = document.querySelector('#categories-dropdown .dropdown-content');
        if (!categoriesDropdown) return;

        categoriesDropdown.innerHTML = '';

        categories.forEach(category => {
            if (category.id === 1) return;

            const categorySlug = this.slugify(category.nombre);

            const categoryLink = document.createElement('a');
            categoryLink.href = `/WEB/categoria.html?categoria=${encodeURIComponent(categorySlug)}`;
            categoryLink.className = 'dropdown-category';
            categoryLink.textContent = category.nombre;
            categoryLink.dataset.categoryId = category.id;

            categoriesDropdown.appendChild(categoryLink);
        });
    }

    updateCategoryInfo() {
        const categoryTitle = document.getElementById('category-title');
        const categoryDescription = document.getElementById('category-description');

        if (this.currentCategoryObj) {
            const title = this.currentCategoryObj.nombre;
            categoryTitle.textContent = title;
            categoryDescription.textContent = `Explora nuestra colección de ${title.toLowerCase()}.`;
        } else {
            categoryTitle.textContent = 'Todos los Productos';
            categoryDescription.textContent = 'Descubre toda nuestra colección de diseños 3D únicos.';
        }
    }
    applyFilters() {
    this.filteredProducts = this.allProducts.filter(product => {
        // No filtrar por categoría porque ya lo hace el backend

        // Filtro precio por rango
        const price = parseFloat(product.precio);

        const min = this.filters.priceMin != null ? this.filters.priceMin : 0;
        const max = this.filters.priceMax != null ? this.filters.priceMax : Infinity;

        if (price < min || price > max) return false;

        return true;
    });

    this.applySorting();
    this.renderProducts();
}




    applySorting() {
        this.filteredProducts.sort((a, b) => {
            const productA = a;  // ahora el objeto es el producto directamente
            const productB = b;

            if (!productA && !productB) return 0;
            if (!productA) return 1;
            if (!productB) return -1;

            switch (this.sortBy) {
                case 'name':
                    return productA.nombre.localeCompare(productB.nombre);
                case 'price-low':
                    return parseFloat(productA.precio) - parseFloat(productB.precio);
                case 'price-high':
                    return parseFloat(productB.precio) - parseFloat(productA.precio);
                case 'newest':
                    return productB.id - productA.id;
                default:
                    return 0;
            }
        });
    }



    renderProducts() {
        const productsGrid = document.getElementById('category-products-grid');
        const emptyState = document.getElementById('empty-state');
        const productsCount = document.getElementById('products-count');

        if (!productsGrid) {
            console.error('No se encontró el contenedor de productos con id "category-products-grid"');
            return;
        }

        // Mostrar contenedor
        productsGrid.style.display = 'grid';

        // Limpiar grid
        productsGrid.innerHTML = '';

        const count = this.filteredProducts.length;
        productsCount.textContent = `${count} producto${count !== 1 ? 's' : ''} encontrado${count !== 1 ? 's' : ''}`;

        if (count === 0) {
            emptyState.style.display = 'block';
            productsGrid.style.display = 'none'; // ocultar grid si está vacío
            return;
        } else {
            emptyState.style.display = 'none';
        }

        // Renderizar productos
        this.filteredProducts.forEach(productDTO => {
            const productCard = this.createProductCard(productDTO);
            productsGrid.appendChild(productCard);
        });
    }

normalizeProductDTO(productDTO) {
    if ('producto' in productDTO) {
        return {
            id: productDTO.producto.id,
            nombre: productDTO.producto.nombre,
            descripcion: productDTO.producto.descripcion,
            precio: productDTO.producto.precio,
            categoriaId: productDTO.producto.categoriaId,
            archivos: productDTO.archivoPrincipal ? [productDTO.archivoPrincipal] : [],
            colores: productDTO.colores || [],
        };
    } else {
        // ProductoSimpleDTO o ProductoResponseDTO del backend
        let archivos = [];
        if (productDTO.archivos && productDTO.archivos.length > 0) {
            archivos = productDTO.archivos;
        } else if (productDTO.linkArchivo) {
            archivos = [{ linkArchivo: productDTO.linkArchivo }];
        }
        return {
            id: productDTO.id,
            nombre: productDTO.nombre,
            descripcion: '',
            precio: productDTO.precio,
            categoriaId: productDTO.categoriaId || null,
            archivos,
            colores: productDTO.colores || [],
        };
    }
}




    createProductCard(productDTO) {
        const producto = productDTO;  // ya es el producto directamente

        // archivos ahora está directo en producto.archivos
        const archivos = producto.archivos;
        const archivoPrincipal = (archivos && archivos.length > 0) ? archivos[0] : null;
        const imageUrl = archivoPrincipal?.linkArchivo || 'https://dummyimage.com/400x400/cccccc/000000&text=Sin+Imagen';


        const card = document.createElement('div');
        card.className = 'category-product-card';
        card.dataset.productId = producto.id;

        const isNew = producto.id > 10;

        card.innerHTML = `
            <div class="category-product-image">
                <img 
                    src="${imageUrl}" 
                    alt="${producto.nombre || 'Producto'}"
                    loading="lazy"
                >
                ${isNew ? '<div class="category-product-badge">NUEVO</div>' : ''}
            </div>
            <div class="category-product-info">
                <h3 class="category-product-name">${producto.nombre || 'Producto sin nombre'}</h3>
                <p class="category-product-price">$${this.formatPrice(producto.precio || 0)}</p>
            </div>
        `;

        card.addEventListener('click', () => {
            window.location.href = `producto.html?id=${producto.id}`;
        });

        return card;
    }

    formatPrice(price) {
        return parseFloat(price).toLocaleString('es-AR', {
            minimumFractionDigits: 0,
            maximumFractionDigits: 0
        });
    }

    initEventListeners() {
        // Filter change handlers
        document.addEventListener('change', async (e) => {
            if (e.target.name === 'category') {
                const selectedCategoryId = Number(e.target.value);
                this.filters.category = e.target.value; // mantener como string por 'all'
                this.currentCategoryId = selectedCategoryId;

                const selectedCategory = this.categories?.find(cat => cat.id === selectedCategoryId);
                this.currentCategoryName = selectedCategory ? this.slugify(selectedCategory.nombre) : '';
                this.currentCategoryObj = selectedCategory || null;
                
                await this.loadProducts();
                this.renderCategoryFilters(this.categories); // actualizar inputs
                this.updateCategoryInfo();

            } else if (e.target.name === 'price') {
                this.filters.price = e.target.value;
                this.applyFilters();

            } else if (e.target.name === 'status') {
                this.filters.status = e.target.value;
                this.applyFilters();
            }
        });

        // Sort change handler
        const sortSelect = document.getElementById('sort-select');
        if (sortSelect) {
            sortSelect.addEventListener('change', (e) => {
                this.sortBy = e.target.value;
                this.applySorting();
                this.renderProducts();
            });
        }
        const applyPriceBtn = document.getElementById('apply-price-filter');
        const priceMinInput = document.getElementById('price-min');
        const priceMaxInput = document.getElementById('price-max');

        if (applyPriceBtn && priceMinInput && priceMaxInput) {
            applyPriceBtn.addEventListener('click', () => {
                const min = parseFloat(priceMinInput.value) || 0;
                const max = parseFloat(priceMaxInput.value) || Infinity;

                this.filters.priceMin = min;
                this.filters.priceMax = max;
                this.applyFilters();
            });
        }
        // Dropdown functionality
        this.initializeDropdown();

    }


    initializeDropdown() {
        const shopTrigger = document.getElementById('shop-trigger');
        const categoriesDropdownMenu = document.getElementById('categories-dropdown');

        if (shopTrigger && categoriesDropdownMenu) {
            shopTrigger.addEventListener('mouseenter', () => {
                categoriesDropdownMenu.classList.add('show');
            });
            
            const navDropdown = shopTrigger.parentElement;
            navDropdown.addEventListener('mouseleave', () => {
                categoriesDropdownMenu.classList.remove('show');
            });
        }
    }

    showLoading() {
        const loadingState = document.getElementById('loading-state');
        const productsGrid = document.getElementById('category-products-grid');
        const emptyState = document.getElementById('empty-state');
        
        if (loadingState) loadingState.style.display = 'block';
        if (productsGrid) productsGrid.style.display = 'none';
        if (emptyState) emptyState.style.display = 'none';
    }

    hideLoading() {
        const loadingState = document.getElementById('loading-state');
        const productsGrid = document.getElementById('category-products-grid');
        
        if (loadingState) loadingState.style.display = 'none';
        if (productsGrid) productsGrid.style.display = 'grid';
    }

    showEmptyState() {
        const emptyState = document.getElementById('empty-state');
        const productsGrid = document.getElementById('category-products-grid');
        
        if (emptyState) emptyState.style.display = 'block';
        if (productsGrid) productsGrid.style.display = 'none';
    }

    hideEmptyState() {
        const emptyState = document.getElementById('empty-state');
        
        if (emptyState) emptyState.style.display = 'none';
    }
}

// Global function to clear filters
function clearFilters() {
    // Reset all radio buttons to "all"
    document.querySelectorAll('input[name="category"][value="all"]')[0].checked = true;
    document.querySelectorAll('input[name="price"][value="all"]')[0].checked = true;
    document.querySelectorAll('input[name="status"][value="all"]')[0].checked = true;
    
    // Reset sort
    const sortSelect = document.getElementById('sort-select');
    if (sortSelect) {
        sortSelect.value = 'name';
    }
    
    // Trigger filter update
    if (window.categoryPage) {
        window.categoryPage.filters = {
            category: 'all',
            price: 'all',
            status: 'all'
        };
        window.categoryPage.sortBy = 'name';
        window.categoryPage.applyFilters();
    }
}


document.addEventListener("DOMContentLoaded", async () => {
    const categoryPage = new CategoryPage();
    window.categoryPage = categoryPage;
    await categoryPage.init();

    // Revisar si hay query de búsqueda
    const searchData = JSON.parse(localStorage.getItem('searchData'));
    const searchQuery = localStorage.getItem('searchQuery');

    if (searchData || searchQuery) {
        try {
            let productsResponse;

            if (searchData) {
                // Filtros avanzados
                const filtros = {
                    q: searchData.query || '',
                    precioMin: searchData.minPrice || 0,
                    precioMax: searchData.maxPrice || Infinity,
                    categoriaId: searchData.category || null
                };
                productsResponse = await API.searchProductsAdvanced(filtros);
            } else {
                // Búsqueda simple
                productsResponse = await API.searchProducts(searchQuery);
            }

            const products = productsResponse.map(p => categoryPage.normalizeProductDTO(p));

            categoryPage.allProducts = products;
            categoryPage.filteredProducts = [...products];

            categoryPage.filters.category = 'all';
            categoryPage.currentCategoryId = null;
            categoryPage.currentCategoryObj = null;

            categoryPage.updateCategoryInfo();
            categoryPage.applyFilters();

        } catch (error) {
            console.error('Error buscando productos por query/filtros:', error);
        }

        localStorage.removeItem('searchData');
        localStorage.removeItem('searchQuery');
    }
});
