// color-manager.js
window.colorManager = (() => {
  // Referencias DOM
  const inputColorText = document.getElementById("input-color-text");
  const inputColorNombre = document.getElementById("input-color-nombre");
  const btnAgregarColor = document.getElementById("btn-agregar-color");
  const listaColores = document.getElementById("lista-colores");

  // Inicializar estado global si no existe
  window.productoState = window.productoState || {};
  window.productoState.coloresSeleccionados = window.productoState.coloresSeleccionados || [];

  // Función para convertir cualquier color a HEX
  function colorToHex(colorStr) {
    const ctx = document.createElement("canvas").getContext("2d");
    ctx.fillStyle = colorStr;
    return ctx.fillStyle;
  }

  // Función que renderiza la lista de colores
  function actualizarListaColores() {
    if (!listaColores) return;

    listaColores.innerHTML = "";
    window.productoState.coloresSeleccionados.forEach((colorObj, index) => {
      const li = document.createElement("li");
      li.style.display = "flex";
      li.style.alignItems = "center";
      li.style.justifyContent = "space-between";
      li.style.marginBottom = "6px";
      li.style.padding = "5px 10px";
      li.style.borderRadius = "4px";
      li.style.backgroundColor = "#f0f0f0";

      // Recuadro del color
      const colorBox = document.createElement("div");
      colorBox.style.width = "20px";
      colorBox.style.height = "20px";
      colorBox.style.borderRadius = "4px";
      colorBox.style.marginRight = "10px";
      colorBox.style.backgroundColor = colorObj.hex;

      // Texto con nombre o HEX
      const span = document.createElement("span");
      span.textContent = colorObj.nombre;
      span.style.flexGrow = "1";

      // Botón borrar
      const btnBorrar = document.createElement("button");
      btnBorrar.textContent = "x";
      btnBorrar.style.backgroundColor = "#dc3545";
      btnBorrar.style.color = "#fff";
      btnBorrar.style.border = "none";
      btnBorrar.style.borderRadius = "50%";
      btnBorrar.style.width = "20px";
      btnBorrar.style.height = "20px";
      btnBorrar.style.cursor = "pointer";
      btnBorrar.addEventListener("click", () => {
        window.productoState.coloresSeleccionados.splice(index, 1);
        actualizarListaColores();
      });

      li.appendChild(colorBox);
      li.appendChild(span);
      li.appendChild(btnBorrar);
      listaColores.appendChild(li);
    });
  }

  // Función para agregar un color
  function agregarColor() {
    if (!inputColorText) return;

    const colorInput = inputColorText.value.trim();
    if (!colorInput) return;

    let hex;
    try {
      hex = colorToHex(colorInput);
    } catch {
      alert("Color inválido. Puede ser HEX, RGB o HSL");
      return;
    }

    const nombre = inputColorNombre.value.trim() || colorInput;
    window.productoState.coloresSeleccionados.push({ hex: hex.toUpperCase(), nombre });
    actualizarListaColores();

    // Actualizar Pickr si existe
    if (window.pickrInstance) window.pickrInstance.setColor(hex.toUpperCase());

    inputColorNombre.value = "";
    inputColorText.value = "";
  }

  // Event listener del botón
  if (btnAgregarColor) {
    btnAgregarColor.addEventListener("click", agregarColor);
  }

  // Inicializar lista si ya hay colores
  actualizarListaColores();

  // Exponer funciones globalmente
  return {
    agregarColor,
    actualizarListaColores
  };
})();
