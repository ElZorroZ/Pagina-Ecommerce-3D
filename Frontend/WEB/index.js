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
