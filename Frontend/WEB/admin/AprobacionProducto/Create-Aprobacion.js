
document.addEventListener("DOMContentLoaded", () => {
(() => {
  const preview = document.getElementById('preview-imagenes'); // ✅ agregado

  // Estado global
  window.productoState = window.productoState || {};
  window.productoState.coloresSeleccionados = window.productoState.coloresSeleccionados || [];
  window.productoState.archivosSeleccionados = window.productoState.archivosSeleccionados || [];


async function subirArchivoBackend(productoId, file, orden) {
  const formData = new FormData();
  formData.append("file", file); // clave que espera Spring
  formData.append("orden", orden);

  try {
    // ✅ usamos authManager para agregar el token
    const res = await authManager.fetchWithAuth(
      `${API_BASE_URL}/api/productos/${productoId}/archivos`,
      {
        method: "POST",
        body: formData,
      }
    );

    if (!res.ok) {
      const errorText = await res.text();
      console.error("Error backend text:", errorText);
      try {
        const errorData = JSON.parse(errorText);
        mostrarError(errorData.message || "Error subiendo archivo al backend");
        throw new Error(errorData.message || "Error subiendo archivo al backend");
      } catch {
        mostrarError(errorText || "Error subiendo archivo al backend");
        throw new Error(errorText || "Error subiendo archivo al backend");
      }
    }

  } catch (err) {
    console.error("Error en subirArchivoBackend:", err);
    mostrarError(err.message || "Error inesperado al subir archivo");
    throw err;
  }
}

  const form = document.getElementById("form-producto");
  const listaColores = document.getElementById("lista-colores");

  
    function actualizarListaColores() {
    listaColores.innerHTML = "";
    window.productoState.coloresSeleccionados.forEach((color) => {
        const li = document.createElement("li");
        li.textContent = color;
        listaColores.appendChild(li);
    });
    }
async function aprobarProducto(id, codigoInicial, versionStr, seguimiento) {
  const url = new URL(`${API_BASE_URL}/api/productosAprobacion/AprobarProducto`);
  url.searchParams.append("id", id);
  url.searchParams.append("codigoInicial", codigoInicial);
  url.searchParams.append("versionStr", versionStr);
  url.searchParams.append("seguimiento", seguimiento);

  try {
    // ✅ usamos authManager para incluir token
    const res = await authManager.fetchWithAuth(url.toString(), { method: "POST" });

    if (!res.ok) {
      const errorText = await res.text();
      console.error("Error backend text:", errorText);
      try {
        const errorData = JSON.parse(errorText);
        mostrarError(errorData.message || "Error al aprobar producto");
        throw new Error(errorData.message || "Error al aprobar producto");
      } catch {
        mostrarError(errorText || "Error al aprobar producto");
        throw new Error(errorText || "Error al aprobar producto");
      }
    }

    // ✅ éxito
    mostrarExito("Producto aprobado correctamente");
  } catch (err) {
    console.error("Error en aprobarProducto:", err);
    mostrarError(err.message || "Error inesperado al aprobar producto");
    throw err;
  }
}


 function actualizarPreview() {
  preview.innerHTML = "";
  if (!window.productoState.archivosSeleccionados || window.productoState.archivosSeleccionados.length === 0) return;

  window.productoState.archivosSeleccionados.forEach((archivo) => {
    const div = document.createElement("div");
    div.style.position = "relative";
    div.style.display = "inline-block";
    div.style.marginRight = "10px";

    const img = document.createElement("img");
    img.style.width = "80px";
    img.style.height = "80px";
    img.style.objectFit = "cover";
    img.style.border = "1px solid #ccc";
    img.style.borderRadius = "4px";

    if (archivo instanceof File) {
      const reader = new FileReader();
      reader.onload = (e) => {
        img.src = e.target.result;
      };
      reader.readAsDataURL(archivo);
    } else {
      img.src = archivo.linkArchivo || archivo.url || "ruta_default.jpg";
    }

    div.appendChild(img);
    preview.appendChild(div);
  });
}

  const inputArchivoComprimido = document.getElementById("archivo-comprimido");
  const previewComprimido = document.getElementById("comprimido-preview");

  inputArchivoComprimido.addEventListener("change", function () {
    const archivo = this.files[0];
    const extensionesPermitidas = [".zip", ".rar", ".7z", ".tar", ".gz", ".bz2"];
    if (archivo && extensionesPermitidas.some(ext => archivo.name.endsWith(ext))) {
      window.productoState.archivoComprimido = archivo;
      actualizarPreviewComprimido();
    } else {
      alert("El archivo debe ser uno de estos: .zip,.rar,.7z,.tar,.gz,.bz2");
      this.value = ""; // limpiar input
    }
  });

 function actualizarPreviewComprimido() {
  previewComprimido.innerHTML = "";
  const archivo = window.productoState.archivoComprimido;
  if (!archivo) return;

  const div = document.createElement("div");
  div.style.position = "relative";
  div.style.display = "inline-block";
  div.style.marginRight = "10px";
  div.style.padding = "6px 10px";
  div.style.border = "1px solid #ccc";
  div.style.borderRadius = "4px";
  div.style.background = "#f9f9f9";
  div.style.fontFamily = "monospace";

  const nombreArchivo = document.createElement("span");
  nombreArchivo.textContent = archivo.name;

  div.appendChild(nombreArchivo);
  previewComprimido.appendChild(div);
}


  form.addEventListener("submit", async (e) => {
  e.preventDefault();

  const nombre = document.getElementById("nombre").value.trim();
  const descripcion = document.getElementById("descripcion").value.trim();
  const precio = parseFloat(document.getElementById("precio").value);
    const precioDigital = parseFloat(document.getElementById("precioDigital").value); // nuevo
  const codigoInicial = document.getElementById("codigo-inicial").value.trim();
  const versionInput = document.getElementById('version');
  const versionValue = versionInput.value.trim();
  const version = versionValue;
  const seguimiento = document.getElementById("seguimiento").value.trim();

  const dimensionAlto = parseInt(document.getElementById("dimension-alto").value);
  const dimensionAncho = parseInt(document.getElementById("dimension-ancho").value);
  const dimensionProfundidad = parseInt(document.getElementById("dimension-profundidad").value);

  const material = document.getElementById("material").value.trim();
  const peso = parseFloat(document.getElementById("peso").value);
  const tecnica = document.getElementById("tecnica").value.trim();


  const categoriaId = parseInt(document.getElementById("categoria").value);

  const productoId = document.getElementById("producto-id").value;

  try {
  mostrarCarga("Aprobando producto..."); // Mostrar overlay
  const backendBase = `${API_BASE_URL}/api/productosAprobacion/AprobarProducto`;

  const url = new URL(backendBase);
  url.searchParams.append('id', productoId); // Reemplazar por el ID real
  url.searchParams.append('codigoInicial', codigoInicial);
  url.searchParams.append('versionStr', version);
  url.searchParams.append('seguimiento', seguimiento);

  const res = await authManager.fetchWithAuth(url.toString(), {
    method: "POST",
  });

  if (!res.ok) {
    const errorText = await res.text();
    mostrarError(errorText || "Error al aprobar producto");
    throw new Error(errorText || "Error al aprobar producto");
  }

  mostrarExito("Producto aprobado con éxito!");
  form.reset();
  window.productoState.coloresSeleccionados = [];
  window.productoState.archivosSeleccionados = [];
  actualizarListaColores();
  actualizarPreview();
  cargarProductos();

} catch (error) {
  console.error("Error al aprobar producto:", error);
  mostrarError("Error: " + error.message);
} finally {
        ocultarCarga(); // Ocultar overlay siempre
    }


});

})();

});
