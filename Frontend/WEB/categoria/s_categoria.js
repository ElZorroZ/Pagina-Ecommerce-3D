const btnMenu = document.querySelector('.btn-menu');
const catalogMenu = document.querySelector('.catalog-menu');

btnMenu.addEventListener('click', () => {
  catalogMenu.classList.toggle('open');
});

// Opcional: cerrar menú si clickeas fuera
catalogMenu.addEventListener('click', (e) => {
  if (e.target === catalogMenu) {
    catalogMenu.classList.remove('open');
  }
});
// Obtener categoriaId de la URL
const urlParams = new URLSearchParams(window.location.search);
const categoriaId = urlParams.get('categoria');

// Seleccionar el contenedor
const productGrid = document.querySelector('.product-grid');
const paginationContainer = document.getElementById('pagination-controls');

function renderProductos(pageData) {
  if (!productGrid) {
    console.error('No se encontró el contenedor de productos (.product-grid)');
    return;
  }
  const productos = pageData.content || [];
  const totalPages = pageData.totalPages || 0;
  const currentPage = pageData.number || 0;

  productGrid.innerHTML = '';

  if (productos.length === 0) {
    productGrid.innerHTML = '<p>No hay productos para esta categoría.</p>';
    if (paginationContainer) paginationContainer.innerHTML = '';
    return;
  }

  productos.forEach((item, index) => {
  let producto;
  let archivoPrincipal;
  let colores;

  if (item.producto) {
    producto = item.producto;
    archivoPrincipal = item.archivoPrincipal;
    colores = item.colores || [];
  } else {
    producto = item;
    archivoPrincipal = producto.archivos && producto.archivos.length > 0 ? producto.archivos[0] : null;
    colores = producto.colores || [];
  }

  const imagen = archivoPrincipal?.linkArchivo || '/img/default.jpeg';

  const card = document.createElement('div');
  card.className = 'product-card';

  card.innerHTML = `
    <div class="product-info" onclick="window.location.href='/WEB/categoria/producto/s_producto.html?id=${producto.id}'">
      <img src="${imagen}" alt="${producto.nombre}" />
      <h3>${producto.nombre}</h3>
      <p class="product-desc">${producto.descripcion}</p>
    </div>

    <div class="product-controls">
      <label for="formato${index}">Formato:</label>
      <select id="formato${index}" class="minimal-select">
        <option value="Archivo">STL</option>
        <option value="Fisico">Fisico</option>
      </select>

      <label for="color${index}">Color:</label>
      <select id="color${index}" class="minimal-select">
        ${colores.map(color => `<option value="${color}">${color}</option>`).join('')}
      </select>

      <p class="product-price">$${producto.precio ? producto.precio.toFixed(2) : '0.00'}</p>

      <div class="product-actions">
        <input type="number" min="1" value="1" class="quantity-input" />
        <button class="add-to-cart-btn">Agregar</button>
      </div>
    </div>
  `;

  productGrid.appendChild(card);
});

  renderPaginacion(currentPage, totalPages, pageData);
}


function renderPaginacion(currentPage, totalPages, pageData) {
  const nextPageBtn = document.querySelector('#next-page-btn');
  console.log('Botón siguiente:', document.getElementById('next-page-btn'));

if (!nextPageBtn) return;
if (pageData.totalElements > 20) {
  nextPageBtn.style.display = 'block';
  nextPageBtn.onclick = () => {
    const nextPage = Math.min(currentPage + 1, totalPages - 1);
    cargarPagina(nextPage);
  };
} else {
  nextPageBtn.style.display = 'none';
}
  if (!paginationContainer) {
    console.error('No se encontró el contenedor de paginación (#pagination-controls)');
    return;
  }

  paginationContainer.innerHTML = '';

  for (let i = 0; i < totalPages; i++) {
    const btn = document.createElement('button');
    btn.textContent = i + 1;
    btn.disabled = i === currentPage;
    btn.addEventListener('click', () => cargarPagina(i));
    paginationContainer.appendChild(btn);
  }
}

function cargarPagina(page) {
  const size = 20;
  let endpoint = '';

  if (categoriaId === 'all') {
    endpoint = `http://localhost:8080/api/productos/todos?page=${page}&size=${size}`;
  } else if (categoriaId) {
    // IMPORTANTE: usar query param categoriaId porque tu backend así lo espera
    endpoint = `http://localhost:8080/api/productos?categoriaId=${categoriaId}&page=${page}&size=${size}`;
  } else {
    // Si no hay categoría, cargar todos los productos paginados
    console.warn('No se especificó categoría, cargando productos completos.');
    endpoint = `http://localhost:8080/api/productos/completo?page=${page}&size=${size}`;
  }

  fetch(endpoint)
    .then(res => {
      if (!res.ok) throw new Error('Error al obtener productos');
      return res.json();
    })
    .then(data => {
      renderProductos(data);
    })
    .catch(err => {
      console.error(err);
      if (productGrid) productGrid.innerHTML = '<p>Error al cargar productos.</p>';
      if (paginationContainer) paginationContainer.innerHTML = '';
    });
}

// Llamada inicial para cargar la primera página cuando DOM esté listo
document.addEventListener('DOMContentLoaded', () => {
  cargarPagina(0);
});
