const searchBtn = document.getElementById('search-btn');
const searchInput = document.getElementById('search-input');
const suggestionsBox = document.getElementById('suggestions-box');

searchBtn.addEventListener('click', () => {
    searchInput.classList.toggle('visible');
    if (searchInput.classList.contains('visible')) {
        searchInput.focus();
    } else {
        searchInput.value = '';
        suggestionsBox.classList.remove('visible');
    }
});

async function handleSearch() {
    const query = searchInput.value.trim();
    if (!query) return;

    const products = await API.searchProducts(query);

    if (products.length === 1) {
        window.location.href = `/producto.html?id=${products[0].id}`; // usar directamente id
    } else if (products.length > 1) {
        localStorage.setItem('searchQuery', query);
        window.location.href = '/categoria.html';
    } else {
        alert("No se encontraron productos con esa búsqueda.");
    }
}


searchInput.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') handleSearch();
});

searchInput.addEventListener('input', async (e) => {
    const query = e.target.value.trim();
    if (!query) {
        suggestionsBox.classList.remove('visible');
        return;
    }

    console.log('🔹 Buscando sugerencias para:', query);
    const suggestions = await API.getSuggestions(query);
    console.log('🔹 Sugerencias recibidas:', suggestions);
    renderSuggestions(suggestions);
});

function renderSuggestions(suggestions) {
    suggestionsBox.innerHTML = '';

    if (!suggestions || suggestions.length === 0) {
        suggestionsBox.classList.remove('visible');
        return;
    }

    suggestions.forEach(s => {
        const item = document.createElement('div');
        item.textContent = s.nombre; // mostramos el nombre
        item.className = 'suggestion-item';
        item.addEventListener('click', () => {
            window.location.href = `/producto.html?id=${s.id}`; // ya no s.producto.id
        });

        suggestionsBox.appendChild(item);
    });

    suggestionsBox.classList.add('visible');
}

// Cerrar sugerencias si se hace clic fuera
document.addEventListener('click', (e) => {
    if (!searchInput.contains(e.target) && !searchBtn.contains(e.target)) {
        suggestionsBox.classList.remove('visible');
    }
});