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
   function setMainImage(src) {
      document.getElementById("main-image").src = src;
    }
    document.addEventListener('DOMContentLoaded', () => {
  const mainImage = document.querySelector('.product-image-main');
  const thumbnails = document.querySelectorAll('.product-thumbnails img');

  thumbnails.forEach(thumb => {
    thumb.addEventListener('click', () => {
      if (thumb.src === mainImage.src) return; // Si ya es la imagen, no hacer nada

      // Agregamos la clase fade-out para iniciar la animación de opacidad
      mainImage.classList.add('fade-out');

      // Cuando termine la animación de fade-out, cambiamos la imagen y hacemos fade-in
      mainImage.addEventListener('transitionend', function handler() {
        mainImage.src = thumb.src;
        mainImage.classList.remove('fade-out');
        // Quitamos el event listener para que no se repita varias veces
        mainImage.removeEventListener('transitionend', handler);
      });

      // Cambiamos el borde para el thumbnail seleccionado
      thumbnails.forEach(t => t.classList.remove('selected'));
      thumb.classList.add('selected');
    });
  });
});
document.addEventListener('DOMContentLoaded', () => {
  const mainImage = document.getElementById('main-image');
  const thumbnailsContainer = document.querySelector('.product-thumbnails');
  const detalleDimension = document.getElementById('detalle-dimension');
  const detalleMaterial = document.getElementById('detalle-material');
  const detalleTecnica = document.getElementById('detalle-tecnica');
  const detallePeso = document.getElementById('detalle-peso');
  const nombreElem = document.getElementById('producto-nombre');
  const descripcionElem = document.getElementById('producto-descripcion');
  const precioElem = document.getElementById('producto-precio');
  const opcionesFormato = document.getElementById('opciones-formato');
  const opcionesColor = document.getElementById('opciones-color');
  const relacionadosContainer = document.getElementById('productos-relacionados');

  const urlParams = new URLSearchParams(window.location.search);
  const productoId = urlParams.get('id');

  if (!productoId) {
    alert('No se especificó producto');
    return;
  }

  function setMainImage(src) {
    if (mainImage.src === src) return;

    mainImage.classList.add('fade-out');
    mainImage.addEventListener('transitionend', function handler() {
      mainImage.src = src;
      mainImage.classList.remove('fade-out');
      mainImage.removeEventListener('transitionend', handler);
    });
  }

  fetch(`http://localhost:8080/api/productos/${productoId}`)
    .then(res => {
      if (!res.ok) throw new Error('Producto no encontrado');
      return res.json();
    })
    .then(data => {
      const prod = data.producto;
      if (!prod) throw new Error('Datos incompletos');

      // Imágenes
      if (data.archivos && data.archivos.length > 0) {
        // Ordenar por campo orden para que respete el orden
        data.archivos.sort((a, b) => a.orden - b.orden);

        mainImage.src = data.archivos[0].linkArchivo || '/img/default.jpeg';

        thumbnailsContainer.innerHTML = '';
        data.archivos.forEach((archivo, i) => {
          const img = document.createElement('img');
          img.src = archivo.linkArchivo || '/img/default.jpeg';
          img.alt = `Imagen ${i+1}`;
          if (i === 0) img.classList.add('selected');
          img.style.cursor = 'pointer';

          img.addEventListener('click', () => {
            setMainImage(img.src);
            thumbnailsContainer.querySelectorAll('img').forEach(t => t.classList.remove('selected'));
            img.classList.add('selected');
          });

          thumbnailsContainer.appendChild(img);
        });
      } else {
        mainImage.src = '/img/default.jpeg';
      }

      // Datos básicos
      nombreElem.textContent = prod.nombre || '-';
      descripcionElem.textContent = prod.descripcion || '-';
      precioElem.textContent = `$${(prod.precio ?? 0).toFixed(2)}`;

      // Detalles combinando dimensiones en una línea o separadas:
      let dimensionText = '-';
      if (prod.dimensionAlto || prod.dimensionAncho || prod.dimensionProfundidad) {
        const dimAlt = prod.dimensionAlto ? prod.dimensionAlto : '';
        const dimAnc = prod.dimensionAncho ? ` x ${prod.dimensionAncho}` : '';
        const dimProf = prod.dimensionProfundidad ? ` x ${prod.dimensionProfundidad}` : '';
        dimensionText = `${dimAlt}${dimAnc}${dimProf}`;
      }
      detalleDimension.textContent = dimensionText;
      detalleMaterial.textContent = prod.material || '-';
      detalleTecnica.textContent = prod.tecnica || '-';
      detallePeso.textContent = prod.peso || '-';

      // Opciones Formato fijas
      opcionesFormato.innerHTML = `
        <label><input type="radio" name="formato" value="Archivo" checked> Archivo</label>
        <label><input type="radio" name="formato" value="Impresión"> Impresión</label>
      `;

      // Opciones Color desde array plano data.colores
      opcionesColor.innerHTML = '';
      if (data.colores && data.colores.length > 0) {
        data.colores.forEach((color, i) => {
          const label = document.createElement('label');
          const input = document.createElement('input');
          input.type = 'radio';
          input.name = 'color';
          input.value = color;
          if (i === 0) input.checked = true;
          label.appendChild(input);
          label.appendChild(document.createTextNode(color));
          opcionesColor.appendChild(label);
        });
      } else {
        opcionesColor.innerHTML = `<label>No hay colores disponibles</label>`;
      }

      // Productos relacionados (mismo categoriaId, distinto id)
      if (prod.categoriaId) {
        fetch(`http://localhost:8080/api/productos?categoriaId=${prod.categoriaId}&page=0&size=10`)
          .then(res => {
            if (!res.ok) throw new Error('Error al cargar productos relacionados');
            return res.json();
          })
          .then(page => {
            relacionadosContainer.innerHTML = '';
            if (page.content && page.content.length > 0) {
              page.content.forEach(item => {
                if (item.id === prod.id) return; // saltar el producto actual

                const card = document.createElement('div');
                card.className = 'product-card';

                // Imagen
                const imagen = (item.archivos && item.archivos.length > 0) ? item.archivos[0].linkArchivo : '/img/default.jpeg';

                card.innerHTML = `
                  <div class="product-info" onclick="window.location.href='/WEB/categoria/producto/s_producto.html?id=${item.id}'">
                    <img src="${imagen}" alt="${item.nombre}" />
                    <h3>${item.nombre}</h3>
                    <p class="product-desc">${item.descripcion || ''}</p>
                  </div>

                  <div class="product-controls">
                    <label for="formato${item.id}">Formato:</label>
                    <select id="formato${item.id}" class="minimal-select">
                        <option value="Archivo">STL</option>
                        <option value="Fisico">Fisico</option>
                    </select>

                    <label for="color${item.id}">Color:</label>
                    <select id="color${item.id}" class="minimal-select">
                      ${(item.colores || []).map(c => `<option value="${c}">${c}</option>`).join('')}
                    </select>

                    <p class="product-price">$${(item.precio ?? 0).toFixed(2)}</p>

                    <div class="product-actions">
                      <input type="number" min="1" value="1" class="quantity-input" />
                      <button class="add-to-cart-btn">Agregar</button>
                    </div>
                  </div>
                `;

                relacionadosContainer.appendChild(card);
              });
            } else {
              // No hay productos relacionados, ocultar sección completa
              relacionadosSection.style.display = 'none';
            }
          })
          .catch(err => {
            console.error(err);
            relacionadosContainer.innerHTML = '<p>Error al cargar productos relacionados.</p>';
            relacionadosSection.style.display = 'none';  // ocultar sección en error también
          });
      } else {
         relacionadosSection.style.display = 'none';  // ocultar si no hay categoría
      }
    })
    .catch(err => {
      alert(err.message);
    });
});
