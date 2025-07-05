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
    fetch("http://localhost:8080/api/productos/completo")
      .then(response => {
        if (!response.ok) throw new Error("No se pudieron cargar los productos");
        return response.json();
      })
      .then(data => cargarProductos(data))
      .catch(error => console.error("Error al cargar productos:", error));
  });

  function cargarProductos(productos) {
    const contenedor = document.getElementById("productos-container");
    contenedor.innerHTML = ""; // Limpiar el contenedor

    productos.forEach(item => {
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