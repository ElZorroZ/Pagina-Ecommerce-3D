document.addEventListener('DOMContentLoaded', () => {
  const listaCategorias = document.getElementById('categoria-lista');
  const masCategorias = document.getElementById('mas-categorias');
  const dropdownMenu = masCategorias ? masCategorias.querySelector('.dropdown-menu') : null;
  const catalogMenu = document.querySelector('.catalog-menu ul');

  const adminPanel = document.getElementById('adminPanel');
  const adminDropdownMenu = adminPanel ? adminPanel.querySelector('.dropdown-menu') : null;

  if (!listaCategorias || !dropdownMenu || !catalogMenu || !masCategorias || !adminPanel || !adminDropdownMenu) return;

  fetch('http://localhost:8080/api/categoria/combo')
    .then(res => {
      if (!res.ok) throw new Error('Error al obtener categorías');
      return res.json();
    })
    .then(categorias => {
      categorias = categorias.filter(c => c.id !== 1);

      const destacadas = categorias.filter(c => c.destacado);
      const normales = categorias.filter(c => !c.destacado);

      const crearLiCategoria = (cat) => {
        const li = document.createElement('li');
        const a = document.createElement('a');
        a.href = '/WEB/categoria/s_categoria.html?categoria=' + encodeURIComponent(cat.id);
        a.textContent = cat.nombre;
        li.appendChild(a);
        return li;
      };

      [...listaCategorias.children].forEach(li => {
        if (li.id !== 'mas-categorias' && li.id !== 'adminPanel') li.remove();
      });

      destacadas.slice(0, 3).forEach(cat => {
        const li = crearLiCategoria(cat);
        listaCategorias.insertBefore(li, masCategorias);
      });

      dropdownMenu.innerHTML = '';
      normales.forEach(cat => {
        dropdownMenu.appendChild(crearLiCategoria(cat));
      });

      catalogMenu.innerHTML = '';
      categorias.forEach(cat => {
        catalogMenu.appendChild(crearLiCategoria(cat));
      });

      // Toggle dropdown "Más categorías"
      masCategorias.querySelector('a').addEventListener('click', (e) => {
        e.preventDefault();
        if (dropdownMenu.style.display === 'block') {
          dropdownMenu.style.display = 'none';
        } else {
          dropdownMenu.style.display = 'block';
          // Cerrar adminPanel si está abierto
          adminDropdownMenu.style.display = 'none';
        }
      });

      // Toggle dropdown "Panel de Administración"
      adminPanel.querySelector('a').addEventListener('click', (e) => {
        e.preventDefault();
        if (adminDropdownMenu.style.display === 'block') {
          adminDropdownMenu.style.display = 'none';
        } else {
          adminDropdownMenu.style.display = 'block';
          // Cerrar "Más categorías" si está abierto
          dropdownMenu.style.display = 'none';
        }
      });

      // Cerrar dropdowns si clickean afuera
      document.addEventListener('click', (e) => {
        if (!masCategorias.contains(e.target)) {
          dropdownMenu.style.display = 'none';
        }
        if (!adminPanel.contains(e.target)) {
          adminDropdownMenu.style.display = 'none';
        }
      });

    })
    .catch(err => {
      console.error('Error cargando categorías:', err);
    });
});
