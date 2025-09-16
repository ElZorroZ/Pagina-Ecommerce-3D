document.addEventListener("DOMContentLoaded", () => {
  (() => {
    // --- Referencias DOM ---
    const form = document.getElementById("form-producto");
    const inputImagenes = document.getElementById('imagenes');
    const preview = document.getElementById('preview-imagenes');
    const inputArchivoComprimido = document.getElementById("archivo-comprimido");
    const previewComprimido = document.getElementById("comprimido-preview");

    // --- Estado global ---
    window.productoState = window.productoState || {};
    window.productoState.coloresSeleccionados = window.productoState.coloresSeleccionados || [];
    window.productoState.archivosSeleccionados = window.productoState.archivosSeleccionados || [];
    window.productoState.archivoComprimido = window.productoState.archivoComprimido || null;

    // --- Color Manager ---
    window.colorManager = window.colorManager || {};
    window.colorManager.colorToHex = (colorStr) => {
      const ctx = document.createElement("canvas").getContext("2d");
      ctx.fillStyle = colorStr;
      return ctx.fillStyle.toUpperCase();
    };
    window.colorManager.agregarColor = (colorStr, nombreStr) => {
      if (!colorStr) return;
      let hex;
      try { hex = window.colorManager.colorToHex(colorStr); } 
      catch { alert("Color inválido. Puede ser HEX, RGB o HSL"); return; }
      const nombre = nombreStr?.trim() || colorStr;
      window.productoState.coloresSeleccionados.push({ hex, nombre });
      window.colorManager.actualizarListaColores();
      if (window.pickrInstance) window.pickrInstance.setColor(hex);
    };
    window.colorManager.actualizarListaColores = () => {
      const listaColores = document.getElementById("lista-colores");
      if (!listaColores) return;
      listaColores.innerHTML = "";
      window.productoState.coloresSeleccionados.forEach((c, idx) => {
        const li = document.createElement("li");
        li.style.cssText = "display:flex; align-items:center; justify-content:space-between; margin-bottom:6px; padding:5px 10px; border-radius:4px; background:#f0f0f0";
        const colorBox = document.createElement("div");
        colorBox.style.cssText = `width:20px;height:20px;border-radius:4px;margin-right:10px;background-color:${c.hex}`;
        const span = document.createElement("span");
        span.textContent = c.nombre;
        span.style.flexGrow = "1";
        const btnBorrar = document.createElement("button");
        btnBorrar.textContent = "x";
        btnBorrar.style.cssText = "background:#dc3545;color:#fff;border:none;border-radius:50%;width:20px;height:20px;cursor:pointer";
        btnBorrar.addEventListener("click", () => {
          window.productoState.coloresSeleccionados.splice(idx, 1);
          window.colorManager.actualizarListaColores();
        });
        li.append(colorBox, span, btnBorrar);
        listaColores.appendChild(li);
      });
    };
    const btnAgregarColor = document.getElementById("btn-agregar-color");
    const inputColorText = document.getElementById("input-color-text");
    const inputColorNombre = document.getElementById("input-color-nombre");
    if (btnAgregarColor) {
      btnAgregarColor.addEventListener("click", () => {
        window.colorManager.agregarColor(inputColorText.value, inputColorNombre.value);
        inputColorText.value = "";
        inputColorNombre.value = "";
      });
    }

    // --- Previews de imágenes ---
if (inputImagenes) {
  inputImagenes.addEventListener("change", () => {
    const files = Array.from(inputImagenes.files);

    files.forEach(file => {
      const existe = window.productoState.archivosSeleccionados.some(f => f.name === file.name && f.size === file.size);

      if (existe) {
        mostrarError(`La imagen "${file.name}" ya fue seleccionada.`);
      } else if (window.productoState.archivosSeleccionados.length >= 5) {
        mostrarError("No se pueden seleccionar más de 5 imágenes.");
      } else {
        window.productoState.archivosSeleccionados.push(file);
      }
    });

    window.actualizarPreview();
    inputImagenes.value = "";
  });
}

window.actualizarPreview = () => {
  if (!preview) return;
  preview.innerHTML = "";

  window.productoState.archivosSeleccionados.forEach((archivo, idx) => {
    const div = document.createElement("div");
    div.style.position = "relative";
    div.style.display = "inline-block";
    div.style.marginRight = "10px";

    const img = document.createElement("img");
    img.style.cssText = "width:80px;height:80px;object-fit:cover;border:1px solid #ccc;border-radius:4px";

    if (archivo instanceof File) {
      const reader = new FileReader();
      reader.onload = (e) => img.src = e.target.result;
      reader.readAsDataURL(archivo);
    } else img.src = archivo.linkArchivo || archivo.url || "ruta_default.jpg";

    const btnEliminar = document.createElement("button");
    btnEliminar.textContent = "X";
    btnEliminar.style.cssText = "position:absolute;top:0;right:0;background:rgba(255,0,0,0.7);color:#fff;border:none;cursor:pointer;border-radius:0 4px 0 4px;padding:0 4px";
    btnEliminar.title = "Eliminar imagen";
    btnEliminar.addEventListener("click", () => {
      window.productoState.archivosSeleccionados.splice(idx, 1);
      window.actualizarPreview();
    });

    div.append(img, btnEliminar);
    preview.appendChild(div);
  });
};

  // --- Preview archivo comprimido ---
    if (inputArchivoComprimido) {
      inputArchivoComprimido.addEventListener("change", function () {
        const archivo = this.files[0];
        const extPermitidas = [".zip", ".rar", ".7z", ".tar", ".gz", ".bz2"];

        if (archivo && extPermitidas.some(ext => archivo.name.toLowerCase().endsWith(ext))) {
          // Guardar en estado global
          window.productoState.archivoComprimido = archivo;

          // Actualizar preview
          if (window.actualizarPreviewComprimido) window.actualizarPreviewComprimido();

          // Mostrar el nombre del archivo seleccionado en la UI
          const preview = document.getElementById("preview-archivo-comprimido");
          if (preview) preview.textContent = "Archivo seleccionado: " + archivo.name;

        } else {
          mostrarError("Archivo inválido. Debe ser: " + extPermitidas.join(", "));
          this.value = "";

          // Limpiar preview si hay
          const preview = document.getElementById("preview-archivo-comprimido");
          if (preview) preview.textContent = "";
        }
      });
    }

    window.actualizarPreviewComprimido = () => {
      if (!previewComprimido) return;
      previewComprimido.innerHTML = "";
      const archivo = window.productoState.archivoComprimido;
      if (!archivo) return;
      const div = document.createElement("div");
      div.style.cssText = "position:relative;display:inline-block;margin-right:10px;padding:6px 10px;border:1px solid #ccc;border-radius:4px;background:#f9f9f9;font-family:monospace";
      const span = document.createElement("span");
      span.textContent = archivo.name;
      const btnEliminar = document.createElement("button");
      btnEliminar.textContent = "X";
      btnEliminar.style.cssText = "position:absolute;top:0;right:0;background:rgba(255,0,0,0.7);color:white;border:none;cursor:pointer;border-radius:0 4px 0 4px;padding:0 4px";
      btnEliminar.addEventListener("click", () => {
        window.productoState.archivoComprimido = null;
        inputArchivoComprimido.value = "";
        window.actualizarPreviewComprimido();
      });
      div.append(span, btnEliminar);
      previewComprimido.appendChild(div);
    };

    // --- Submit del form ---
if (form) {
    form.addEventListener("submit", async (e) => {
    mostrarCarga("Guardando producto..."); // Mostrar overlay
      e.preventDefault(); // evita recargar la página

      // --- Recolectar campos ---
      const nombre = document.getElementById("nombre").value.trim();
      const descripcion = document.getElementById("descripcion").value.trim();
      const categoriaId = parseInt(document.getElementById("categoria").value);
      const precio = parseFloat(document.getElementById("precio").value);
      const precioDigitalInput = document.getElementById("precioDigital");
      if (precioDigitalInput) {
        precioDigitalInput.value = producto.precioDigital || "";
      }
      const codigoInicial = document.getElementById("codigo-inicial").value.trim();
      const version = document.getElementById("version").value.trim();
      const seguimiento = document.getElementById("seguimiento").value.trim();
      const dimensionAlto = parseFloat(document.getElementById("dimension-alto").value);
      const dimensionAncho = parseFloat(document.getElementById("dimension-ancho").value);
      const dimensionProfundidad = parseFloat(document.getElementById("dimension-profundidad").value);
      const material = document.getElementById("material").value.trim();
      const peso = parseFloat(document.getElementById("peso").value);
      const tecnica = document.getElementById("tecnica").value.trim();

      if (!nombre || !categoriaId || isNaN(precio)) {
        mostrarError("Por favor completa todos los campos obligatorios.");
        return;
      }
      // 🔹 Validar que no haya imágenes de otros productos
      const archivosInvalidos = window.productoState.archivosSeleccionados.filter(f => !(f instanceof File));
      if (archivosInvalidos.length > 0) {
        mostrarError("No se puede guardar el producto con imágenes de otro producto. Elimina las imágenes antiguas.");
        return;
      }
      try {
        const productoPayload = {
          nombre, descripcion, categoriaId, precio, precioDigital,
          colores: window.productoState.coloresSeleccionados,
          codigoInicial, version, seguimiento,
          dimensionAlto, dimensionAncho, dimensionProfundidad,
          material, peso, tecnica
        };

        // 🔹 MOSTRAR DTO ANTES DE ENVIAR
        console.log("DTO que se enviará:", productoPayload);

        // --- Crear producto (POST) ---
        const formData = new FormData();
        formData.append("producto", new Blob([JSON.stringify(productoPayload)], { type: "application/json" }));

        // Agregar archivo comprimido
        if (window.productoState.archivoComprimido) formData.append("archivo", window.productoState.archivoComprimido);

        const res = await authManager.fetchWithAuth(`${API_BASE_URL}/api/productos`, {
          method: "POST",
          body: formData
        });

        if (!res.ok) {
          const errorText = await res.text();
          throw new Error(errorText);
        }

        const productoCreado = await res.json();
        const productoId = productoCreado.id;

        // --- Subir imágenes al endpoint de archivos ---
        for (let i = 0; i < window.productoState.archivosSeleccionados.length; i++) {
          const file = window.productoState.archivosSeleccionados[i];
          const imgFormData = new FormData();
          imgFormData.append("file", file);
          imgFormData.append("orden", i + 1);

          try {
            const resImg = await authManager.fetchWithAuth(`${API_BASE_URL}/api/productos/${productoId}/archivos`, {
              method: "POST",
              body: imgFormData
            });

            if (!resImg.ok) {
              const errorText = await resImg.text();
              console.error("Error subiendo imagen:", file.name, errorText);
            }
          } catch (err) {
            console.error("Error subiendo imagen:", file.name, err);
          }
        }

        mostrarExito("Producto guardado con éxito!");

        // --- Limpiar estado y previews ---
        form.reset();
        window.productoState.coloresSeleccionados = [];
        window.productoState.archivosSeleccionados = [];
        window.productoState.archivoComprimido = null;
        window.colorManager.actualizarListaColores();
        window.cargarProductos();
        window.actualizarPreview();
        window.actualizarPreviewComprimido();

      } catch (err) {
    console.error(err);

    // Intentar extraer solo el mensaje del servidor
    let mensaje = "Error al guardar el producto";
      if (err.response) {
        // Si tu fetch devuelve un objeto con .response
        if (err.response.message) mensaje = err.response.message;
      } else {
        // Intentar extraer texto plano que venga en err.message
        const match = /Error al crear el producto:\s*(.*)/i.exec(err.message);
        if (match && match[1]) {
          mensaje = match[1]; // solo el mensaje útil
        } else if (err.message) {
          mensaje = err.message; // fallback
        }
      }

      mostrarError(mensaje); // Mostrarlo limpio al usuario
    }
  finally {
          ocultarCarga(); // Ocultar overlay siempre
      }

  });
}



    // --- Inicializar lista de colores ---
    window.colorManager.actualizarListaColores();

  })();
});
