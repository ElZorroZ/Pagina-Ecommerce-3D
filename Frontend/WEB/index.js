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
document.addEventListener("DOMContentLoaded", () => {
  const miniHeroesContainer = document.querySelector('.mini-heroes');
  const heroContainer = document.querySelector('.hero-container');

  if (!miniHeroesContainer || !heroContainer) return;

  fetch('http://localhost:8080/api/categoria/combo')
    .then(res => {
      if (!res.ok) throw new Error('Error al obtener categorías');
      return res.json();
    })
    .then(categorias => {
      // Filtrar solo las categorías destacadas para mini-heroes
      const destacadas = categorias.filter(cat => cat.destacado && cat.id !== 1);

      // Buscar la categoría con id === 1 para hero principal
      const heroPrincipal = categorias.find(cat => cat.id === 1);

      // Limpiar contenido previo (si existe)
      miniHeroesContainer.innerHTML = '';

      // Crear y agregar cada mini-hero
      destacadas.forEach(cat => {
        const miniHero = document.createElement('div');
        miniHero.className = 'mini-hero';

        miniHero.innerHTML = `
          <a href="/WEB/categoria/s_categoria.html?categoria=${encodeURIComponent(cat.id)}">  
            <img src="${cat.linkArchivo || '/img/default.jpeg'}" alt="${cat.nombre}" />
            <div class="mini-overlay"></div>
          </a>
          <div class="mini-hero-text">${cat.nombre}</div>
        `;

        miniHeroesContainer.appendChild(miniHero);
      });

      // Actualizar hero principal si existe
      if (heroPrincipal) {
        heroContainer.innerHTML = `
          <a href="/WEB/categoria/s_categoria.html?categoria=all" class="hero-link">
            <img src="${heroPrincipal.linkArchivo || '/img/default.jpeg'}" alt="${heroPrincipal.nombre}" class="hero-img" />
            <div class="hero-overlay"></div>
          </a>
        `;
      }
    })
    .catch(err => {
      console.error('Error cargando categorías:', err);
    });

    //Cargar destacados
    fetch("http://localhost:8080/api/productos/completo")
      .then(response => {
        if (!response.ok) throw new Error("No se pudieron cargar los productos");
        return response.json();
      })
      .then(data => cargarProductos(data))
      .catch(error => console.error("Error al cargar productos:", error));
  });

function cargarProductos(productos) {
  const contenedor = document.getElementById('contenedor-productos');
  contenedor.innerHTML = '';

  productos.content.forEach(item => {
    const producto = item.producto;
    const imagen = item.archivoPrincipal?.linkArchivo || "/img/default.jpeg";
    const colores = item.colores || ["Negro", "Blanco"];

    const card = document.createElement("div");
    card.classList.add("product-card");

    card.innerHTML = `
      <div class="product-info" onclick="window.location.href='/producto/${producto.id}'">
        <img src="${imagen}" alt="${producto.nombre}" />
        <h3>${producto.nombre}</h3>
        <p class="product-desc">${producto.descripcion}</p>
      </div>

      <div class="product-controls">
        <label>Formato:</label>
        <select class="minimal-select">
          <option value="Archivo">Archivo</option>
          <option value="Impresion">Impresión</option>
        </select>

        <label>Color:</label>
        <select class="minimal-select">
          ${colores.map(color => `<option>${color}</option>`).join("")}
        </select>

        <p class="product-price">$${producto.precio.toFixed(2)}</p>

        <div class="product-actions">
          <input type="number" min="1" value="1" class="quantity-input" />
          <button class="add-to-cart-btn">Agregar</button>
        </div>
      </div>
    `;

    contenedor.appendChild(card);
  });
}
