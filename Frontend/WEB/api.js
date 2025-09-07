// API Base URL - replace with your actual backend URL
const API_BASE_URL = 'https://forma-programada.onrender.com';

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


    async searchProducts(query) {
    try {
        console.log('🔹 fetch simple:', `${API_BASE_URL}/api/productos/busqueda/simple?q=${encodeURIComponent(query)}`);
        const response = await fetch(`${API_BASE_URL}/api/productos/busqueda/simple?q=${encodeURIComponent(query)}`);
        const products = await response.json();
        console.log('🔹 response simple:', products);
        return products || [];  // ya es lista de ProductoSimpleDTO
    } catch (error) {
        console.error('Error searching products:', error);
        return [];
    }
},


    async searchProductsAdvanced(filtros, ordenarPor = 'relevancia', page = 0, size = 20) {
        try {
            console.log('🔹 fetch avanzada:', `${API_BASE_URL}/api/productos/busqueda/avanzada?page=${page}&size=${size}&ordenarPor=${ordenarPor}`);
            const response = await fetch(`${API_BASE_URL}/api/productos/busqueda/avanzada?page=${page}&size=${size}&ordenarPor=${ordenarPor}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(filtros),
            });
            const data = await response.json();
            console.log('🔹 response avanzada:', data);
            return data.content || [];
        } catch (error) {
            console.error('Error searching products advanced:', error);
            return [];
        }
    },

    async getSuggestions(query) {
        try {
            console.log('🔹 fetch sugerencias:', `${API_BASE_URL}/api/productos/busqueda/sugerencias?q=${encodeURIComponent(query)}`);
            const response = await fetch(`${API_BASE_URL}/api/productos/busqueda/sugerencias?q=${encodeURIComponent(query)}`);
            const suggestions = await response.json();
            console.log('🔹 response sugerencias:', suggestions);
            return suggestions || [];
        } catch (error) {
            console.error('Error fetching suggestions:', error);
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
},
async enviarReview(reviewData) {
  const usuarioId = localStorage.getItem('usuarioId'); // opcional, depende si tu backend lo necesita
  if (!usuarioId) {
    throw new Error('No se encontró usuarioId en localStorage');
  }

  try {
    const response = await fetch(`${API_BASE_URL}/api/reviews`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('accessToken')}` // si usás auth
      },
      body: JSON.stringify(reviewData)
    });

    if (!response.ok) {
      let errorMsg = response.statusText;
      try {
        errorMsg = await response.text();
      } catch {}
      throw new Error(`Error al enviar la reseña: ${errorMsg}`);
    }

    const result = await response.json();
    return result; // devuelve el ReviewResponseDTO que retorna el backend

  } catch (error) {
    console.error('Error en enviarReview API:', error);
    throw error;
  }
},
async obtenerReviews(productId) {
    try {
        const response = await fetch(`${API_BASE_URL}/api/reviews/producto/${productId}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
                // No enviamos Authorization porque es público
            }
        });

        if (!response.ok) {
            let errorMsg = response.statusText;
            try {
                errorMsg = await response.text();
            } catch {}
            throw new Error(`Error al obtener reviews: ${errorMsg}`);
        }

        const reviews = await response.json();
        return reviews;
    } catch (error) {
        console.error('Error en obtenerReviews API:', error);
        throw error;
    }
},
async responderReview(reviewId, requestBody) {
    try {
        const token = localStorage.getItem('accessToken'); // tu JWT

        const response = await fetch(`${API_BASE_URL}/api/reviews/${reviewId}/responder`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}` // necesario para COLABORADOR o ADMIN
            },
            body: JSON.stringify(requestBody)
        });

        if (!response.ok) {
            let errorMsg = response.statusText;
            try {
                const errorData = await response.json();
                if (errorData && errorData.message) errorMsg = errorData.message;
            } catch {}
            throw new Error(`Error al responder review: ${errorMsg}`);
        }

        const result = await response.json();
        return result; // ReviewResponseDTO
    } catch (error) {
        console.error('Error en responderReview API:', error);
        throw error;
    }
    
},
async eliminarReview(reviewId) {
    try {
        const token = localStorage.getItem('accessToken'); // tu JWT

        const response = await fetch(`${API_BASE_URL}/api/reviews/${reviewId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}` // necesario para ADMIN
            }
        });

        if (!response.ok) {
            let errorMsg = response.statusText;
            try {
                const errorData = await response.json();
                if (errorData && errorData.message) errorMsg = errorData.message;
            } catch {}
            throw new Error(`Error al eliminar review: ${errorMsg}`);
        }

        // El endpoint retorna 204 No Content, no hay body que parsear
        return true;
    } catch (error) {
        console.error('Error en eliminarReview API:', error);
        throw error;
    }
},
 async obtenerUsuarioPorToken() {
        try {
            const token = localStorage.getItem('accessToken');
            if (!token) throw new Error('No se encontró token de acceso');

            // Decodificar JWT para obtener el sub
            const payloadBase64 = token.split('.')[1];
            const payloadJson = atob(payloadBase64.replace(/-/g, '+').replace(/_/g, '/'));
            const payload = JSON.parse(payloadJson);
            const gmail = payload.sub;
            if (!gmail) throw new Error('Token inválido, no tiene sub');

            // Llamada al endpoint
            const response = await fetch(`${API_BASE_URL}/api/usuario/${gmail}`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                let errorMsg = response.statusText;
                try {
                    const errorData = await response.json();
                    if (errorData && errorData.message) errorMsg = errorData.message;
                } catch {}
                throw new Error(`Error al obtener usuario: ${errorMsg}`);
            }

            const data = await response.json();
            return data; // UsuarioGetUpdateResponse
        } catch (error) {
            console.error('Error en obtenerUsuarioPorToken API:', error);
            throw error;
        }
    },
    async modificarPedido(usuarioCambios) {
      try {
          const token = localStorage.getItem('accessToken');
          if (!token) throw new Error('No se encontró token de acceso');

          // Decodificar JWT para obtener el sub (gmail)
          const payloadBase64 = token.split('.')[1];
          const payloadJson = atob(payloadBase64.replace(/-/g, '+').replace(/_/g, '/'));
          const payload = JSON.parse(payloadJson);
          const gmail = payload.sub;
          if (!gmail) throw new Error('Token inválido, no tiene sub');

          // Construir body, siempre enviando gmail del token
          const body = {
              ...usuarioCambios,
              gmail
          };

          const response = await fetch(`${API_BASE_URL}/api/pedido/modificarPedido`, {
              method: 'PUT',
              headers: {
                  'Authorization': `Bearer ${token}`,
                  'Content-Type': 'application/json'
              },
              body: JSON.stringify(body)
          });

          if (!response.ok) {
              let errorMsg = response.statusText;
              try {
                  const errorData = await response.text();
                  if (errorData) errorMsg = errorData;
              } catch {}
              throw new Error(`Error al modificar pedido: ${errorMsg}`);
          }

          // No hay body que parsear, éxito
          return true;

      } catch (error) {
          console.error('Error en modificarPedido API:', error);
          throw error;
      }
  },
  async crearPedido(cart) {
  try {
    const token = localStorage.getItem('accessToken'); // tu JWT

    const response = await fetch(`https://forma-programada.onrender.com/api/pedido/crearPedido`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}`
      },
      body: JSON.stringify(cart)
    });

    if (!response.ok) {
      const errorText = await response.text();
      console.error("Error backend:", response.status, errorText);
      throw new Error("Error al crear pedido");
    }

    return await response.json();
  } catch (err) {
    console.error("Error en crearPedido:", err);
    throw err;
  }
}
,


  async confirmarPedido(pedido, quantity) {
    try {
        const token = localStorage.getItem("accessToken");
        if (!token) throw new Error("No se encontró token de acceso");

        const response = await fetch(`${API_BASE_URL}/api/mp/confirmarPedido?quantity=${quantity}`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify(pedido)
        });

        if (!response.ok) {
            let errorMsg = response.statusText;
            try {
                const errorData = await response.json();
                if (errorData && errorData.error) errorMsg = errorData.error;
            } catch {}
            throw new Error(`Error al confirmar pedido: ${errorMsg}`);
        }

        const data = await response.json();
        return data.initPoint; // 👈 URL de Mercado Pago
    } catch (error) {
        console.error("Error en confirmarPedido API:", error);
        throw error;
    }
}



};
window.API = API;
