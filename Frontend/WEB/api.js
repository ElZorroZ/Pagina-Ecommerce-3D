// API functions for categories and products
const API = {
    // Fetch all categories
    async getCategories() {
        try {
            // Si es pública, podemos usar fetch normal o fetchWithAuth
            const response = await fetch(`${API_BASE_URL}/api/categoria`);
            if (!response.ok) throw new Error(`API error (${response.status})`);
            const categories = await response.json();
            return categories;
        } catch (error) {
            console.error('Error cargando categorias:', error);
            return []; // retorno seguro
        }
    },
    async loadProducts() {
        try {
            const response = await fetch(`${API_BASE_URL}/api/productos`);
            if (response.status === 204) {
                console.warn('No hay productos disponibles');
                return;
            }
            if (!response.ok) throw new Error(`Error HTTP: ${response.status}`);

            products = await response.json();
        } catch (err) {
            console.error('Error al cargar productos:', err);
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
            return [];
        }
    },

    // Fetch products by category
    async getProductsByCategory(categoryId) {
        try {
            const response = await fetch(`${API_BASE_URL}/api/productos?categoriaId=${categoryId}`);
            if (!response.ok) throw new Error(`Error HTTP: ${response.status}`);
            const products = await response.json();
            return products;
        } catch (error) {
            console.error('Error cargando productos por categoría:', error);
            return [];
        }
    },

    // Fetch all products (sin filtro de categoría)
    async getAllProducts() {
        try {
            const response = await fetch(`${API_BASE_URL}/api/productos/todos`);
            if (!response.ok) throw new Error(`Error HTTP: ${response.status}`);
            const products = await response.json();
            return products;
        } catch (error) {
            console.error('Error cargando todos los productos:', error);
            return [];
        }
    },

    // Fetch latest product for hero section
    async getLatestProduct() {
        try {
            const response = await fetch(`${API_BASE_URL}/api/productos/ultimo`);
            if (!response.ok) throw new Error(`Error HTTP: ${response.status}`);
            const latestProduct = await response.json();
            return latestProduct;
        } catch (error) {
            console.error('Error cargando el último producto:', error);
            return null;
        }
    },
    // En tu archivo de API, agregar:
    async getAllProductsForSearch() {
        try {
            const response = await fetch(`${API_BASE_URL}/api/productos/todos/busqueda`);
            if (!response.ok) throw new Error(`Error HTTP: ${response.status}`);
            const products = await response.json();
            return products; // Ya es un array directo, no paginado
        } catch (error) {
            console.error('Error cargando productos para búsqueda:', error);
            return [];
        }
    },
    async getCompleteProductById(id) {
        try {
            const response = await fetch(`${API_BASE_URL}/api/productos/${id}`);
            
            if (response.status === 404) {
                console.warn('Producto no encontrado');
                return null;
            }

            if (!response.ok) throw new Error(`Error HTTP: ${response.status}`);

            const product = await response.json();
            return product;
        } catch (error) {
            console.error('Error al obtener el producto:', error);
            return null;
        }
    },

    // Búsqueda simple de productos
    async searchProducts(query) {
        try {
            const url = `${API_BASE_URL}/api/productos/busqueda/simple?q=${encodeURIComponent(query)}`;
            console.log('🔹 fetch simple:', url);
            const response = await fetch(url);
            if (!response.ok) throw new Error(`Error HTTP: ${response.status}`);
            const products = await response.json();
            console.log('🔹 response simple:', products);
            return products || [];
        } catch (error) {
            console.error('Error searching products:', error);
            return [];
        }
    },

    // Búsqueda avanzada de productos
    async searchProductsAdvanced(filtros, ordenarPor = 'relevancia', page = 0, size = 20) {
        try {
            const url = `${API_BASE_URL}/api/productos/busqueda/avanzada?page=${page}&size=${size}&ordenarPor=${ordenarPor}`;
            console.log('🔹 fetch avanzada:', url);

            const response = await fetch(url, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(filtros),
            });

            if (!response.ok) throw new Error(`Error HTTP: ${response.status}`);

            const data = await response.json();
            console.log('🔹 response avanzada:', data);
            return data.content || [];
        } catch (error) {
            console.error('Error searching products advanced:', error);
            return [];
        }
    },

    // Obtener sugerencias para autocompletado
    async getSuggestions(query) {
        try {
            const url = `${API_BASE_URL}/api/productos/busqueda/sugerencias?q=${encodeURIComponent(query)}`;
            console.log('🔹 fetch sugerencias:', url);
            const response = await fetch(url);
            if (!response.ok) throw new Error(`Error HTTP: ${response.status}`);
            const suggestions = await response.json();
            console.log('🔹 response sugerencias:', suggestions);
            return suggestions || [];
        } catch (error) {
            console.error('Error fetching suggestions:', error);
            return [];
        }
    },

    // Agregar producto al carrito
    async agregarProductoACarrito(data) {
        try {
            const response = await authManager.fetchWithAuth(`${API_BASE_URL}/api/carrito/agregarProductoaCarrito`, {
                method: 'POST',
                body: JSON.stringify(data)
            });

            if (!response.ok) {
                const errorText = await response.text().catch(() => response.statusText);
                throw new Error(errorText || "Error al agregar al carrito");
            }

            return await response.json();
        } catch (error) {
            console.error('Error agregando producto al carrito:', error);
            throw error;
        }
    },

    // Sumar cantidad de un producto en el carrito
    async sumarCantidadCarrito(id, cantidad) {
        try {
            const response = await authManager.fetchWithAuth(`${API_BASE_URL}/api/carrito/sumarCantidad/${id}/${cantidad}`, {
                method: 'PUT'
            });

            if (!response.ok) {
                const errorText = await response.text().catch(() => response.statusText);
                throw new Error(errorText || "Error al sumar cantidad");
            }

            return await response.json();
        } catch (error) {
            console.error('Error sumando cantidad en carrito:', error);
            throw error;
        }
    },


        // Aquí la función obtenerCarrito corregida y agregada al objeto API
    async obtenerCarrito() {
    const usuarioId = authManager.getUserId();
    if (!usuarioId) throw new Error('No se encontró usuarioId');

    try {
        const response = await authManager.fetchWithAuth(`${API_BASE_URL}/api/carrito/verCarritoConImagen/${usuarioId}`);
        if (!response.ok) throw new Error(`Error al obtener carrito: ${response.statusText}`);
        return await response.json();
    } catch (error) {
        console.error('Error en obtenerCarrito API:', error);
        throw error;
    }
    },

    async sumarCantidad(id, cantidad) {
    try {
        const response = await authManager.fetchWithAuth(`${API_BASE_URL}/api/carrito/sumarCantidad/${id}/${cantidad}`, {
        method: 'PUT'
        });

        if (!response.ok) throw new Error(`Error en sumarCantidad: ${response.statusText}`);
        return await response.json();
    } catch (error) {
        console.error('Error en sumarCantidad API:', error);
        throw error;
    }
    },

    async borrarProductoCarrito(id) {
    try {
        const response = await authManager.fetchWithAuth(`${API_BASE_URL}/api/carrito/borrarProductoaCarrito/${id}`, {
        method: 'DELETE'
        });

        if (!response.ok) {
        let errorMsg = response.statusText;
        try { errorMsg = await response.text(); } catch {}
        throw new Error(`Error en borrarProductoCarrito: ${errorMsg}`);
        }

        return true;
    } catch (error) {
        console.error('Error en borrarProductoCarrito API:', error);
        throw error;
    }
    },

    async vaciarCarrito() {
    const usuarioId = authManager.getUserId();
    if (!usuarioId) throw new Error('No se encontró usuarioId');

    try {
        const response = await authManager.fetchWithAuth(`${API_BASE_URL}/api/carrito/vaciarCarrito/${usuarioId}`, {
        method: 'DELETE'
        });

        if (!response.ok) {
        let errorMsg = response.statusText;
        try { errorMsg = await response.text(); } catch {}
        throw new Error(`Error al vaciar carrito: ${errorMsg}`);
        }

        return true;
    } catch (error) {
        console.error('Error en vaciarCarrito API:', error);
        throw error;
    }
    },

    async enviarReview(reviewData) {
    try {
        const response = await authManager.fetchWithAuth(`${API_BASE_URL}/api/reviews`, {
        method: 'POST',
        body: JSON.stringify(reviewData)
        });

        if (!response.ok) {
        let errorMsg = response.statusText;
        try { errorMsg = await response.text(); } catch {}
        throw new Error(`Error al enviar la reseña: ${errorMsg}`);
        }

        return await response.json();
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
    // Responder review
    async responderReview(reviewId, requestBody) {
        try {
            const response = await authManager.fetchWithAuth(`${API_BASE_URL}/api/reviews/${reviewId}/responder`, {
                method: 'POST',
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

            return await response.json(); // ReviewResponseDTO
        } catch (error) {
            console.error('Error en responderReview API:', error);
            throw error;
        }
    },

    // Eliminar review
    async eliminarReview(reviewId) {
        try {
            const response = await authManager.fetchWithAuth(`${API_BASE_URL}/api/reviews/${reviewId}`, {
                method: 'DELETE'
            });

            if (!response.ok) {
                let errorMsg = response.statusText;
                try {
                    const errorData = await response.json();
                    if (errorData && errorData.message) errorMsg = errorData.message;
                } catch {}
                throw new Error(`Error al eliminar review: ${errorMsg}`);
            }

            // No hay body, retorna true
            return true;
        } catch (error) {
            console.error('Error en eliminarReview API:', error);
            throw error;
        }
    },

    // Obtener usuario por token
    async obtenerUsuarioPorToken() {
        try {
            // Decodificar JWT localmente
            const token = authManager.getAccessToken();
            if (!token) throw new Error('No se encontró token de acceso');

            const payloadBase64 = token.split('.')[1];
            const payloadJson = atob(payloadBase64.replace(/-/g, '+').replace(/_/g, '/'));
            const payload = JSON.parse(payloadJson);
            const gmail = payload.sub;
            if (!gmail) throw new Error('Token inválido, no tiene sub');

            const response = await authManager.fetchWithAuth(`${API_BASE_URL}/api/usuario/${gmail}`, {
                method: 'GET'
            });

            if (!response.ok) {
                let errorMsg = response.statusText;
                try {
                    const errorData = await response.json();
                    if (errorData && errorData.message) errorMsg = errorData.message;
                } catch {}
                throw new Error(`Error al obtener usuario: ${errorMsg}`);
            }

            return await response.json(); // UsuarioGetUpdateResponse
        } catch (error) {
            console.error('Error en obtenerUsuarioPorToken API:', error);
            throw error;
        }
    },
    // Modificar pedido
    async modificarPedido(usuarioCambios) {
        try {
            // Obtener gmail del token
            const token = authManager.getAccessToken();
            if (!token) throw new Error('No se encontró token de acceso');

            const payloadBase64 = token.split('.')[1];
            const payloadJson = atob(payloadBase64.replace(/-/g, '+').replace(/_/g, '/'));
            const payload = JSON.parse(payloadJson);
            const gmail = payload.sub;
            if (!gmail) throw new Error('Token inválido, no tiene sub');

            const body = { ...usuarioCambios, gmail };

            const response = await authManager.fetchWithAuth(`${API_BASE_URL}/api/pedido/modificarPedido`, {
                method: 'PUT',
                body: JSON.stringify(body)
            });

            if (!response.ok) {
                let errorMsg = await response.text().catch(() => response.statusText);
                throw new Error(`Error al modificar pedido: ${errorMsg}`);
            }

            return true; // éxito
        } catch (error) {
            console.error('Error en modificarPedido API:', error);
            throw error;
        }
    },

    // Crear pedido
    async crearPedido(cart) {
        try {
            const response = await authManager.fetchWithAuth(`${API_BASE_URL}/api/pedido/crearPedido`, {
                method: 'POST',
                body: JSON.stringify(cart)
            });

            if (!response.ok) {
                const errorText = await response.text().catch(() => response.statusText);
                throw new Error(`Error al crear pedido: ${errorText}`);
            }

            return await response.json();
        } catch (error) {
            console.error('Error en crearPedido API:', error);
            throw error;
        }
    },

   // Confirmar pedido (Mercado Pago)
async confirmarPedido(pedido, quantity) {
        console.log("\n🚀 === INICIANDO confirmarPedido ===");
        
        try {
            // Validaciones iniciales
            if (!pedido) {
                throw new Error("Pedido es null o undefined");
            }
            if (!pedido.id) {
                throw new Error("Pedido no tiene ID");
            }
            if (!quantity || quantity <= 0) {
                throw new Error("Quantity inválida: " + quantity);
            }

            const API_BASE_URL = "https://formaprogramada.com.ar";
            const url = `${API_BASE_URL}/api/mp/confirmarPedido?quantity=${quantity}`;
            
            console.log("📍 URL completa:", url);
            console.log("📦 Pedido a enviar:", JSON.stringify(pedido, null, 2));
            console.log("🔢 Quantity:", quantity);
            console.log("🔐 AuthManager disponible:", !!authManager);
            console.log("🎫 Token disponible:", !!authManager.getAccessToken());

            // Verificar estado de autenticación
            if (!authManager.isAuthenticated()) {
                throw new Error("Usuario no autenticado");
            }

            // Preparar request
            const requestBody = JSON.stringify(pedido);
            console.log("📝 Request body:", requestBody);

            const requestOptions = {
                method: 'PUT',
                headers: { 
                    "Content-Type": "application/json"
                },
                body: requestBody
            };

            console.log("⚙️ Request options (antes de auth):", requestOptions);

            // Hacer la petición CON autenticación
            console.log("📡 Enviando request...");
            const response = await authManager.fetchWithAuth(url, requestOptions);

            console.log("📡 Response recibida:");
            console.log("   - Status:", response.status);
            console.log("   - Ok:", response.ok);
            console.log("   - StatusText:", response.statusText);
            console.log("   - Headers:", [...response.headers.entries()]);

            if (!response.ok) {
                let errorMsg;
                try {
                    errorMsg = await response.text();
                } catch (e) {
                    errorMsg = response.statusText;
                }
                console.error("❌ Response error:", errorMsg);
                throw new Error(`Error ${response.status}: ${errorMsg}`);
            }

            const data = await response.json();
            console.log("✅ Response data:", data);
            
            if (!data.initPoint) {
                throw new Error("Response no contiene initPoint: " + JSON.stringify(data));
            }
            
            console.log("🎯 InitPoint obtenido:", data.initPoint);
            return data.initPoint;

        } catch (error) {
            console.error("\n❌ === ERROR EN confirmarPedido ===");
            console.error("Error completo:", error);
            console.error("Error name:", error.name);
            console.error("Error message:", error.message);
            console.error("Error stack:", error.stack);
            console.error("=== FIN ERROR ===\n");
            throw error;
        }
    },
    // Ver pedidos de usuario
    async verPedidosDeUsuario(userId) {
        try {
            const response = await authManager.fetchWithAuth(`${API_BASE_URL}/api/pedido/verPedidosDeUsuario?id=${userId}`, {
                method: 'GET'
            });

            if (!response.ok) {
                let errorMsg = await response.text().catch(() => response.statusText);
                throw new Error(`Error al obtener pedidos: ${errorMsg}`);
            }

            return await response.json(); // array de PedidoDTO
        } catch (error) {
            console.error('Error en verPedidosDeUsuario API:', error);
            throw error;
        }
    }
};
window.API = API;
