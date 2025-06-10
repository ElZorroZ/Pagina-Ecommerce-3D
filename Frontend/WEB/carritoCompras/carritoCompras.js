// Simulamos productos
let carrito = [
  {
    id: 1,
    nombre: "Camiseta",
    imagen: "/img/ejemplo-1.jpeg",
    precio: 2500,
    cantidad: 1
  },
  {
    id: 2,
    nombre: "Gorra",
    imagen: "/img/ejemplo-1.jpeg",
    precio: 1500,
    cantidad: 2
  }
];

const tbody = document.getElementById('carrito-body');
const totalGeneral = document.getElementById('total-general');

function renderCarrito() {
  tbody.innerHTML = "";
  let total = 0;

  carrito.forEach(producto => {
    const subtotal = producto.precio * producto.cantidad;
    total += subtotal;

    const fila = document.createElement('tr');
    fila.innerHTML = `
      <td>
        <div>
          <strong>${producto.nombre}</strong><br>
          <img src="${producto.imagen}" alt="${producto.nombre}">
        </div>
      </td>
      <td>$${producto.precio}</td>
      <td>
        <button onclick="restarCantidad(${producto.id})">−</button>
        ${producto.cantidad}
        <button onclick="aumentarCantidad(${producto.id})">+</button>
        </td>

      <td>
        $${subtotal}
        <br>
        <button class="btn-eliminar" onclick="eliminarProducto(${producto.id})">Eliminar</button>
      </td>
    `;
    tbody.appendChild(fila);
  });

  totalGeneral.textContent = total;
}

function aumentarCantidad(id) {
  const producto = carrito.find(p => p.id === id);
  if (producto) {
    producto.cantidad++;
    renderCarrito();
  }
}
function restarCantidad(id) {
  const producto = carrito.find(p => p.id === id);
  if (producto) {
    if (producto.cantidad > 1) {
      producto.cantidad--;
    } else {
      // Opcional: si la cantidad es 1 y resta, eliminar el producto
      carrito = carrito.filter(p => p.id !== id);
    }
    renderCarrito();
  }
}

function eliminarProducto(id) {
  carrito = carrito.filter(p => p.id !== id);
  renderCarrito();
}

document.getElementById('avanzar-compra').addEventListener('click', () => {
  alert("Redirigiendo a la página de pago...");
  // Acá podrías redirigir o guardar el carrito en localStorage/backend
});

renderCarrito();
