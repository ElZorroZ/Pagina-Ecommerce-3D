// header-categorias.js

document.addEventListener('DOMContentLoaded', () => {
  const listaCategorias = document.getElementById('categoria-lista');
  const masCategorias = document.getElementById('mas-categorias');
  const dropdownMenu = masCategorias ? masCategorias.querySelector('.dropdown-menu') : null;
  const catalogMenu = document.querySelector('.catalog-menu ul');

  if (!listaCategorias || !dropdownMenu || !catalogMenu || !masCategorias) return;

  fetch('http://localhost:8080/api/categoria/combo')
    .then(res => {
      if (!res.ok) throw new Error('Error al obtener categorías');
      return res.json();
    })
    .then(categorias => {
      // Separar destacadas y normales
      const destacadas = categorias.filter(c => c.destacado);
      const normales = categorias.filter(c => !c.destacado);

      // Función para crear un <li> con enlace a categoría
      const crearLiCategoria = (cat) => {
        const li = document.createElement('li');
        const a = document.createElement('a');
        a.href = `/productos?categoria=${encodeURIComponent(cat.id)}`;
        a.textContent = cat.nombre;
        li.appendChild(a);
        return li;
      };

      // Limpiar elementos que no sean 'mas-categorias' o 'adminPanel'
      [...listaCategorias.children].forEach(li => {
        if (li.id !== 'mas-categorias' && li.id !== 'adminPanel') li.remove();
      });

      // Insertar categorías destacadas (máximo 3) antes de 'mas-categorias'
      destacadas.slice(0, 3).forEach(cat => {
        const li = crearLiCategoria(cat);
        listaCategorias.insertBefore(li, masCategorias);
      });

      // Vaciar dropdown y agregar sólo categorías normales (sin destacados)
      dropdownMenu.innerHTML = '';
      normales.forEach(cat => {
        dropdownMenu.appendChild(crearLiCategoria(cat));
      });

      // Vaciar y llenar menú móvil con todas las categorías (destacadas + normales)
      catalogMenu.innerHTML = '';
      categorias.forEach(cat => {
        catalogMenu.appendChild(crearLiCategoria(cat));
      });

      // Toggle dropdown al clickear "Más categorías"
      masCategorias.querySelector('a').addEventListener('click', (e) => {
        e.preventDefault();
        dropdownMenu.style.display = dropdownMenu.style.display === 'block' ? 'none' : 'block';
      });

      // Cerrar dropdown si clickean afuera
      document.addEventListener('click', (e) => {
        if (!masCategorias.contains(e.target)) {
          dropdownMenu.style.display = 'none';
        }
      });
    })
    .catch(err => {
      console.error('Error cargando categorías:', err);
    });
});
