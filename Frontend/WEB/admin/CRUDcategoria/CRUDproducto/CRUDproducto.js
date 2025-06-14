const form = document.getElementById("form-producto");
const tabla = document.querySelector("#tabla-productos tbody");
const previewNombre = document.getElementById("preview-nombre");
const previewDescripcion = document.getElementById("preview-descripcion");
const previewPrecio = document.getElementById("preview-precio");
const previewMain = document.getElementById("preview-main");
const previewThumbnails = document.getElementById("preview-thumbnails");

let productos = [];
let editando = null;

form.addEventListener("submit", (e) => {
  e.preventDefault();

  const nombre = document.getElementById("nombre").value.trim();
  const descripcion = document.getElementById("descripcion").value.trim();
  const precio = parseFloat(document.getElementById("precio").value);
  const imagenPrincipal = document.getElementById("imagenPrincipal").value.trim();
  const imagenesExtra = document.getElementById("imagenesExtra").value.split(",").map(s => s.trim()).filter(Boolean);

  const producto = {
    id: editando ?? Date.now(),
    nombre,
    descripcion,
    precio,
    imagenPrincipal,
    imagenesExtra
  };

  if (editando) {
    productos = productos.map(p => p.id === editando ? producto : p);
    editando = null;
  } else {
    productos.push(producto);
  }

  form.reset();
  renderTabla();
  actualizarPreview(producto);
});

function renderTabla() {
  tabla.innerHTML = "";
  productos.forEach(producto => {
    const fila = document.createElement("tr");
    fila.innerHTML = `
      <td>${producto.nombre}</td>
      <td>${producto.descripcion}</td>
      <td>$${producto.precio.toFixed(2)}</td>
      <td>
        <button onclick="editarProducto(${producto.id})">Editar</button>
        <button onclick="eliminarProducto(${producto.id})">Eliminar</button>
      </td>
    `;
    tabla.appendChild(fila);
  });
}

function editarProducto(id) {
  const producto = productos.find(p => p.id === id);
  if (!producto) return;
  document.getElementById("nombre").value = producto.nombre;
  document.getElementById("descripcion").value = producto.descripcion;
  document.getElementById("precio").value = producto.precio;
  document.getElementById("imagenPrincipal").value = producto.imagenPrincipal;
  document.getElementById("imagenesExtra").value = producto.imagenesExtra.join(", ");
  editando = producto.id;
  actualizarPreview(producto);
}

function eliminarProducto(id) {
  productos = productos.filter(p => p.id !== id);
  renderTabla();
  if (editando === id) {
    editando = null;
    form.reset();
  }
}

function actualizarPreview(producto) {
  previewNombre.textContent = producto.nombre || "Sin nombre";
  previewDescripcion.textContent = producto.descripcion || "Sin descripción";
  previewPrecio.textContent = `$${producto.precio?.toFixed(2) || 0}`;
  previewMain.src = producto.imagenPrincipal || "/img/ejemplo-1.jpeg";

  previewThumbnails.innerHTML = "";
  const todas = [producto.imagenPrincipal, ...producto.imagenesExtra];
  todas.forEach((src, i) => {
    const img = document.createElement("img");
    img.src = src;
    img.alt = `miniatura ${i + 1}`;
    if (i === 0) img.classList.add("selected");
    img.onclick = () => {
      document.querySelectorAll("#preview-thumbnails img").forEach(i => i.classList.remove("selected"));
      img.classList.add("selected");
      previewMain.src = src;
    };
    previewThumbnails.appendChild(img);
  });
}
