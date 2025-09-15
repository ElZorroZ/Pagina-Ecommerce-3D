document.addEventListener('DOMContentLoaded', async () => {
    const searchBtn = document.getElementById('search-btn');
    const searchInput = document.getElementById('search-input');
    const suggestionsBox = document.getElementById('suggestions-box');

    if (!searchBtn || !searchInput || !suggestionsBox) {
        console.error('❌ Elementos de búsqueda no encontrados');
        return;
    }

    let products = [];
    let fuse;
    let searchTimeout;

    // Cargar productos del backend con la estructura correcta
    try {
        if (window.API && typeof window.API.getAllProductsForSearch === 'function') {
            console.log('📡 Cargando productos...');
            const rawProducts = await window.API.getAllProductsForSearch() || [];
            
            console.log('🔍 DEBUGGING - Productos RAW recibidos:', rawProducts.length);
            console.log('📋 DEBUGGING - Estructura de productos RAW:', rawProducts);
            
            // Transformar datos a estructura plana para búsqueda
            const allTransformedProducts = rawProducts.map((item, index) => {
                console.log(`🔍 DEBUGGING - Procesando producto ${index}:`, item);
                
                const transformed = {
                    id: item.producto?.id || item.id,
                    nombre: item.producto?.nombre || item.nombre,
                    descripcion: item.producto?.descripcion || item.descripcion || '',
                    precio: item.producto?.precio || item.precio,
                    categoria: item.producto?.categoriaId || item.categoriaId || '',
                    colores: item.colores || [],
                    archivo: item.archivoPrincipal?.linkArchivo || item.producto?.archivo || item.archivo
                };
                
                console.log(`✅ DEBUGGING - Producto transformado ${index}:`, transformed);
                console.log(`🏷️ DEBUGGING - Nombre del producto ${index}: "${transformed.nombre}"`);
                
                return transformed;
            });
            
            console.log('🔍 DEBUGGING - Total productos transformados:', allTransformedProducts.length);
            console.log('📋 DEBUGGING - Productos con nombre válido:', allTransformedProducts.filter(p => p.nombre && p.nombre.trim()).length);
            console.log('❌ DEBUGGING - Productos SIN nombre válido:', allTransformedProducts.filter(p => !p.nombre || !p.nombre.trim()));
            
            // Filtrar solo productos con nombre válido
            products = allTransformedProducts.filter(p => {
                const hasValidName = p.nombre && p.nombre.trim();
                if (!hasValidName) {
                    console.warn('⚠️ DEBUGGING - Producto sin nombre válido excluido:', p);
                }
                return hasValidName;
            });
            
            console.log('✅ Productos procesados FINAL:', products.length);
            console.log('🔍 Lista de nombres FINAL:', products.map(p => p.nombre));
            
            // Configuración de Fuse.js
            if (products.length > 0) {
                fuse = new Fuse(products, {
                    keys: [
                        { name: 'nombre', weight: 0.8 },
                        { name: 'descripcion', weight: 0.2 }
                    ],
                    threshold: 0.5,
                    distance: 100,
                    minMatchCharLength: 1,
                    includeScore: true,
                    includeMatches: true,
                    ignoreLocation: true,
                    findAllMatches: true,
                    shouldSort: true,
                    tokenize: true
                });
                console.log('✅ Fuse.js configurado correctamente');
            } else {
                console.error('❌ No hay productos válidos para configurar Fuse.js');
            }
        } else {
            console.error('❌ window.API.getAllProducts no está disponible');
        }
    } catch (error) {
        console.error('❌ Error cargando productos:', error);
    }

    // Toggle del input de búsqueda
    searchBtn.addEventListener('click', () => {
        searchInput.classList.toggle('visible');
        if (searchInput.classList.contains('visible')) {
            searchInput.focus();
        } else {
            closeSearch();
        }
    });

    // Búsqueda con Enter
    searchInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            handleSearch();
        }
    });

    // Navegación con teclado
    searchInput.addEventListener('keydown', (e) => {
        const suggestions = suggestionsBox.querySelectorAll('.suggestion-item');
        const activeSuggestion = suggestionsBox.querySelector('.suggestion-item.active');
        let activeIndex = activeSuggestion ? Array.from(suggestions).indexOf(activeSuggestion) : -1;

        switch (e.key) {
            case 'ArrowDown':
                e.preventDefault();
                activeIndex = Math.min(activeIndex + 1, suggestions.length - 1);
                updateActiveSuggestion(suggestions, activeIndex);
                break;
            case 'ArrowUp':
                e.preventDefault();
                activeIndex = Math.max(activeIndex - 1, -1);
                updateActiveSuggestion(suggestions, activeIndex);
                break;
            case 'Escape':
                closeSearch();
                break;
            case 'Enter':
                if (activeSuggestion) {
                    e.preventDefault();
                    const productId = activeSuggestion.dataset.productId;
                    navigateToProduct(productId);
                }
                break;
        }
    });

    // Sugerencias en tiempo real
    searchInput.addEventListener('input', () => {
        clearTimeout(searchTimeout);
        
        const query = searchInput.value.trim();
        
        if (!query || query.length < 1) {
            suggestionsBox.classList.remove('visible');
            return;
        }

        searchTimeout = setTimeout(() => {
            performSearch(query, true);
        }, 150);
    });

    // Cerrar sugerencias al hacer click fuera
    document.addEventListener('click', (e) => {
        if (!searchInput.contains(e.target) && 
            !searchBtn.contains(e.target) && 
            !suggestionsBox.contains(e.target)) {
            suggestionsBox.classList.remove('visible');
        }
    });

    // Función principal de búsqueda
    function handleSearch() {
        const query = searchInput.value.trim();
        if (!query) return;
        performSearch(query, false);
    }

    // Búsqueda unificada
    function performSearch(query, isSuggestion = false) {
        console.log(`🔍 Buscando "${query}" (isSuggestion: ${isSuggestion})`);
        
        if (!products.length) {
            console.log('❌ No hay productos disponibles');
            if (!isSuggestion) {
                alert("No hay productos disponibles para buscar.");
            }
            return;
        }

        // Búsqueda directa case-insensitive (principal)
        const lowerQuery = query.toLowerCase();
        const directMatches = products.filter(product => 
            product.nombre && product.nombre.toLowerCase().includes(lowerQuery)
        );

        console.log(`✅ Matches directos: ${directMatches.length}`);
        if (directMatches.length > 0) {
            console.log('📋 Productos encontrados:', directMatches.map(p => p.nombre));
        }

        // Búsqueda con Fuse.js como respaldo
        let fuseResults = [];
        if (fuse && directMatches.length === 0) {
            fuseResults = fuse.search(query).map(r => ({
                item: r.item,
                score: r.score,
                isDirect: false
            }));
            console.log(`🔍 Resultados Fuse: ${fuseResults.length}`);
        }

        // Combinar resultados
        const allResults = [
            ...directMatches.map(item => ({ item, score: 0, isDirect: true })),
            ...fuseResults
        ];

        // Eliminar duplicados
        const uniqueResults = [];
        const addedIds = new Set();
        
        allResults.forEach(result => {
            if (!addedIds.has(result.item.id)) {
                uniqueResults.push(result);
                addedIds.add(result.item.id);
            }
        });

        console.log(`📊 Total resultados únicos: ${uniqueResults.length}`);

        if (isSuggestion) {
            renderSuggestions(uniqueResults.slice(0, 8), query);
            return;
        }

        // Lógica de búsqueda final
        if (uniqueResults.length === 0) {
            console.log('❌ Sin resultados, intentando búsqueda parcial...');
            
            // Búsqueda parcial por caracteres
            const partialResults = products.filter(product => {
                if (!product.nombre) return false;
                const productName = product.nombre.toLowerCase();
                const searchQuery = lowerQuery;
                
                // Buscar si todos los caracteres del query están en el producto
                return searchQuery.split('').every(char => 
                    productName.includes(char)
                );
            });

            console.log(`🔍 Resultados parciales: ${partialResults.length}`);

            if (partialResults.length > 0) {
                if (partialResults.length === 1) {
                    navigateToProduct(partialResults[0].id);
                } else {
                    localStorage.setItem('searchResults', JSON.stringify(partialResults));
                    localStorage.setItem('searchQuery', query);
                    window.location.href = '/categoria.html';
                }
                return;
            }

            alert(`No se encontraron productos con "${query}".`);
            return;
        }

        // Buscar coincidencia exacta
        const exactMatch = uniqueResults.find(result => 
            result.item.nombre && result.item.nombre.toLowerCase() === lowerQuery
        );

        if (exactMatch) {
            console.log('✅ Coincidencia exacta encontrada:', exactMatch.item.nombre);
            navigateToProduct(exactMatch.item.id);
        } else if (uniqueResults.length === 1) {
            console.log('✅ Resultado único:', uniqueResults[0].item.nombre);
            navigateToProduct(uniqueResults[0].item.id);
        } else {
            console.log('📂 Múltiples resultados, yendo a categoría');
            const searchResults = uniqueResults.map(r => r.item);
            localStorage.setItem('searchResults', JSON.stringify(searchResults));
            localStorage.setItem('searchQuery', query);
            window.location.href = '/categoria.html';
        }
    }

    // Renderizar sugerencias
    function renderSuggestions(results, query) {
        suggestionsBox.innerHTML = '';

        if (!results.length) {
            suggestionsBox.classList.remove('visible');
            return;
        }

        const lowerQuery = query.toLowerCase();

        results.forEach((result) => {
            const item = result.item;
            const suggestionItem = document.createElement('div');
            suggestionItem.className = 'suggestion-item';
            suggestionItem.dataset.productId = item.id;
            
            const highlightedName = highlightMatch(item.nombre, query);
            
            suggestionItem.innerHTML = `
                <div class="suggestion-name">${highlightedName}</div>
                <div class="suggestion-price">$${item.precio?.toLocaleString() || 'N/A'}</div>
            `;

            // Marcar tipos de coincidencias
            if (item.nombre.toLowerCase() === lowerQuery) {
                suggestionItem.classList.add('exact-match');
            } else if (result.isDirect) {
                suggestionItem.classList.add('direct-match');
            }

            suggestionItem.addEventListener('click', () => {
                navigateToProduct(item.id);
            });

            suggestionItem.addEventListener('mouseenter', () => {
                removeActiveFromAll();
                suggestionItem.classList.add('active');
            });

            suggestionsBox.appendChild(suggestionItem);
        });

        suggestionsBox.classList.add('visible');
    }

    // Resaltar coincidencias
    function highlightMatch(text, query) {
        if (!query) return text;
        const escapedQuery = query.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
        const regex = new RegExp(`(${escapedQuery})`, 'gi');
        return text.replace(regex, '<strong>$1</strong>');
    }

    // Utilidades
    function updateActiveSuggestion(suggestions, activeIndex) {
        removeActiveFromAll();
        if (activeIndex >= 0 && suggestions[activeIndex]) {
            suggestions[activeIndex].classList.add('active');
        }
    }

    function removeActiveFromAll() {
        const suggestions = suggestionsBox.querySelectorAll('.suggestion-item');
        suggestions.forEach(s => s.classList.remove('active'));
    }

    function navigateToProduct(productId) {
        window.location.href = `/producto.html?id=${productId}`;
    }

    function closeSearch() {
        searchInput.value = '';
        searchInput.classList.remove('visible');
        suggestionsBox.classList.remove('visible');
        clearTimeout(searchTimeout);
    }

    window.addEventListener('beforeunload', () => {
        clearTimeout(searchTimeout);
    });

    // Función de test para debugging
    window.debugBusqueda = function(query = null) {
        console.log('🔍 DEBUG INFORMACIÓN:');
        console.log('- Total productos:', products.length);
        console.log('- Productos con nombre válido:', products.filter(p => p.nombre).length);
        console.log('- Fuse configurado:', !!fuse);
        console.log('- Lista de nombres:', products.map(p => p.nombre));
        
        if (query) {
            console.log(`\n🧪 PROBANDO BÚSQUEDA: "${query}"`);
            const matches = products.filter(p => 
                p.nombre && p.nombre.toLowerCase().includes(query.toLowerCase())
            );
            console.log('- Matches encontrados:', matches.length);
            console.log('- Productos:', matches.map(p => p.nombre));
        }
    };

    console.log('✅ Motor de búsqueda inicializado. Usa debugBusqueda() para información.');
});