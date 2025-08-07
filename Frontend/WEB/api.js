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
    agregarProductoACarrito: async (data) => {
    const token = localStorage.getItem("accessToken"); // o como lo hayas llamado

    const response = await fetch(`${API_BASE_URL}/api/carrito/agregarProductoaCarrito`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}` // ✅ importante
        },
        body: JSON.stringify(data)
    });

    if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || "Error al agregar al carrito");
    }

    return response.json();
},

sumarCantidadCarrito: async (id, cantidad) => {
    const token = localStorage.getItem("accessToken");

    const response = await fetch(`${API_BASE_URL}/api/carrito/sumarCantidad/${id}/${cantidad}`, {
        method: 'PUT',
        headers: {
            'Authorization': `Bearer ${token}` // ✅ también acá si está protegido
        }
    });

    if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || "Error al sumar cantidad");
    }

    return response.json();
},

     // Aquí la función obtenerCarrito corregida y agregada al objeto API
  async obtenerCarrito() {
    const usuarioId = localStorage.getItem('usuarioId');
    const token = localStorage.getItem('accessToken');

    if (!usuarioId || !token) {
      throw new Error('No hay usuarioId o token en localStorage');
    }

    const response = await fetch(`${API_BASE_URL}/api/carrito/verCarritoConImagen/${usuarioId}`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });

    if (!response.ok) {
      throw new Error(`Error al obtener carrito: ${response.statusText}`);
    }

    const carrito = await response.json();
    return carrito;
  },
  async sumarCantidad(id, cantidad) {
    try {
      const response = await fetch(`${API_BASE_URL}/api/carrito/sumarCantidad/${id}/${cantidad}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('accessToken')}` // si el endpoint requiere auth
        }
      });

      if (!response.ok) {
        throw new Error(`Error en sumarCantidad: ${response.statusText}`);
      }
      const data = await response.json();
      return data;
    } catch (error) {
      console.error('Error en sumarCantidad API:', error);
      throw error;
    }
  },
  async borrarProductoCarrito(id) {
  try {
    const response = await fetch(`${API_BASE_URL}/api/carrito/borrarProductoaCarrito/${id}`, {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
      }
    });

    if (!response.ok) {
      // Si la respuesta tiene contenido (texto o JSON), intenta leerlo para dar mejor error
      let errorMsg = response.statusText;
      try {
        errorMsg = await response.text();
      } catch {}

      throw new Error(`Error en borrarProductoCarrito: ${errorMsg}`);
    }

    // No hagas response.json() porque no hay body
    return true;

  } catch (error) {
    console.error('Error en borrarProductoCarrito API:', error);
    throw error;
  }
},
async vaciarCarrito() {
  const usuarioId = localStorage.getItem('usuarioId');
  if (!usuarioId) {
    throw new Error('No se encontró usuarioId en localStorage');
  }

  try {
    const response = await fetch(`${API_BASE_URL}/api/carrito/vaciarCarrito/${usuarioId}`, {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('accessToken')}` // si usás auth
      }
    });

    if (!response.ok) {
      let errorMsg = response.statusText;
      try {
        errorMsg = await response.text();
      } catch {}
      throw new Error(`Error al vaciar carrito: ${errorMsg}`);
    }

    return true; // o lo que devuelva el backend si querés manejarlo

  } catch (error) {
    console.error('Error en vaciarCarrito API:', error);
    throw error;
  }
}


};
window.API = API;
