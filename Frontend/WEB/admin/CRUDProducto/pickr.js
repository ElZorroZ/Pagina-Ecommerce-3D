// pickr.js
window.pickrInstance = null;

document.addEventListener("DOMContentLoaded", () => {
  const colorPickerEl = document.querySelector('.color-picker');

  if (!colorPickerEl) {
    console.log("No hay elemento .color-picker en esta página");
    return;
  }

  try {
    window.pickrInstance = Pickr.create({
      el: colorPickerEl,
      theme: 'classic',
      default: '#00ff00',
      components: {
        preview: true,
        opacity: true,
        hue: true,
        interaction: {
          hex: true,
          rgba: true,
          hsla: true,
          input: true,
          save: true
        }
      }
    });

    const inputColorText = document.getElementById("input-color-text");
    if (inputColorText) {
      // Cambios en Pickr se reflejan en el input
      window.pickrInstance.on('change', (color) => {
        inputColorText.value = color.toHEXA().toString();
      });

      // Al tocar Save solo actualizamos input y cerramos Pickr
      window.pickrInstance.on('save', (color) => {
        inputColorText.value = color.toHEXA().toString();
        window.pickrInstance.hide();
      });
    }

  } catch (err) {
    console.error("Error inicializando Pickr:", err);
  }
});

