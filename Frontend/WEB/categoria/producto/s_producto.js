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
