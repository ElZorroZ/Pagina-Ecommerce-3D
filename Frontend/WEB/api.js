// API Base URL - replace with your actual backend URL
const API_BASE_URL = 'http://localhost:8080';

// API functions for categories and products
const API = {
    // Fetch all categories
    async getCategories() {
        try {
            const response = await fetch(`${API_BASE_URL}/api/categoria`);
            if (!response.ok) throw new Error(`API error (${response.status})`);
            const text = await response.text();
            if (!text) throw new Error('Respuesta vacía');
            const categories = JSON.parse(text);
            return categories;
        } catch (error) {
            console.error('Error cargando categorias:', error);
        }
    },

    // Fetch all complete products
    async getCompleteProducts() {
        try {
            const response = await fetch(`${API_BASE_URL}/api/productos/completo`);
            
            if (response.status === 204) {
                console.warn('No hay productos disponibles');
                return [];
            }

            if (!response.ok) {
                throw new Error(`Error HTTP: ${response.status}`);
            }

            const products = await response.json();
            return products;
        } catch (error) {
            console.error('Error al cargar productos completos:', error);
        }
    },

    // Fetch products by category
    async getProductsByCategory(categoryId) {
        try {
            const response = await fetch(`${API_BASE_URL}/api/productos?categoriaId=${categoryId}`);
            const products = await response.json();
            return products;
        } catch (error) {
            console.error('Error loading products by category:', error);
            return [];
        }
    },

    // Fetch all products (sin filtro de categoría)
    async getAllProducts() {
        try {
            const response = await fetch(`${API_BASE_URL}/api/productos/todos`);
            const products = await response.json();
            return products;
        } catch (error) {
            console.error('Error loading all products:', error);
            return [];
        }
    },

    // Fetch latest product for hero section
    async getLatestProduct() {
        try {
            const response = await fetch(`${API_BASE_URL}/api/productos/ultimo`);
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            const latestProductDTO = await response.json();
            return latestProductDTO;
        } catch (error) {
            console.error('Error cargando el ultimo producto:', error);
        }
    },

    async getCompleteProductById(id) {
        try {
            const response = await fetch(`${API_BASE_URL}/api/productos/${id}`);
            
            if (response.status === 404) {
                console.warn('Producto no encontrado');
                return null;
            }

            if (!response.ok) {
                throw new Error(`Error HTTP: ${response.status}`);
            }

            const product = await response.json();
            return product;
        } catch (error) {
            console.error('Error al obtener el producto:', error);
            return null;
        }
    },


    // Search products
    async searchProducts(query) {
        try {
            const response = await fetch(`${API_BASE_URL}/products/search?q=${encodeURIComponent(query)}`);
            const products = await response.json();
            return products;
        } catch (error) {
            console.error('Error searching products:', error);
            return [];
        }
    },

    
};