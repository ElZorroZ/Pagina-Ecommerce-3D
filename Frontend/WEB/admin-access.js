    async function validarToken() {
        const accessToken = localStorage.getItem('accessToken');
        const refreshToken = localStorage.getItem('refreshToken');

        if (!accessToken && !refreshToken) {
            return null;
        }

        try {
            let finalAccessToken = accessToken;

            if (!accessToken || tokenExpirado(accessToken)) {
                if (!refreshToken) throw new Error("No hay refresh token");

                const response = await fetch("http://localhost:8080/api/auth/refresh", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ refreshToken }),
                });

                if (!response.ok) throw new Error("Refresh token inválido");

                const data = await response.json();
                localStorage.setItem('accessToken', data.accessToken);
                localStorage.setItem('refreshToken', data.refreshToken);
                finalAccessToken = data.accessToken;
            }

            return finalAccessToken;

        } catch (err) {
            console.error('Error en validación de token:', err);
            localStorage.removeItem('accessToken');
            localStorage.removeItem('refreshToken');
            return null;
        }
    }

    function tokenExpirado(token) {
        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            const now = Math.floor(Date.now() / 1000);
            return payload.exp && payload.exp < now;
        } catch {
            return true;
        }
    }

document.addEventListener('DOMContentLoaded', () => {
    const accountBtn = document.getElementById('account-btn');
    const accountMenu = document.getElementById('account-menu');
    const adminDropdown = document.getElementById("admin-dropdown");
    const adminOptions = document.getElementById("admin-options");
    const logoutBtn = document.getElementById('logout-btn');
    let hoverTimeout;

    if (!accountBtn || !accountMenu) {
        console.warn('No se encontraron accountBtn o accountMenu. Se detiene el script.');
        return;
    }

    const ocultarDropdownConDelay = () => {
        hoverTimeout = setTimeout(() => {
            accountMenu.classList.remove('show');
        }, 200);
    };

    const cancelarOcultamiento = () => clearTimeout(hoverTimeout);

    accountMenu.addEventListener('mouseleave', ocultarDropdownConDelay);
    accountMenu.addEventListener('mouseenter', cancelarOcultamiento);
    accountBtn.addEventListener('mouseleave', ocultarDropdownConDelay);
    accountBtn.addEventListener('mouseenter', cancelarOcultamiento);

    document.addEventListener('click', (event) => {
        if (!accountMenu.contains(event.target) && !accountBtn.contains(event.target)) {
            accountMenu.classList.remove('show');
        }
    });

    if (logoutBtn) {
        logoutBtn.addEventListener('click', (e) => {
            e.preventDefault();
            localStorage.removeItem('accessToken');
            localStorage.removeItem('refreshToken');
            localStorage.removeItem('usuarioId');  
            location.reload();
        });
    }

    async function verificarAccesoAdmin() {
        const accessToken = localStorage.getItem("accessToken");
        if (!accessToken || !adminDropdown || !adminOptions) return;

        try {
            const payloadBase64 = accessToken.split('.')[1];
            const payload = JSON.parse(atob(payloadBase64));
            const roles = payload.roles || [];

            if (roles.includes("ROLE_ADMIN") || roles.includes("ROLE_COLABORADOR")) {
                adminDropdown.classList.remove("hidden"); // Mostrar el contenedor
                
                // Limpiar y agregar las opciones
                adminOptions.innerHTML = "";

                if (roles.includes("ROLE_ADMIN")) {
                    adminOptions.innerHTML = `
                        <a href="/WEB/admin/CRUDCategoria/CRUDcategoria.html" class="dropdown-category">Gestionar categorías</a>
                        <a href="/WEB/admin/CRUDProducto/CRUDproducto.html" class="dropdown-category">Gestionar productos</a>
                        <a href="/admin/pedidos.html" class="dropdown-category">Gestionar pedidos</a>
                        <a href="/WEB/admin/Aprobacion/CRUDaprobacion.html" class="dropdown-category">Gestionar colaboradores</a>
                        <a href="/admin/productos-pendientes.html" class="dropdown-category">Gestionar productos sin aprobar</a>
                    `;
                } else {
                    adminOptions.innerHTML = `
                        <a href="/admin/productos-pendientes.html" class="dropdown-category">Gestionar productos</a>
                    `;
                }

                initializeAdminDropdownHover();

            } else {
                adminDropdown.classList.add("hidden"); // Ocultar el contenedor
            }
        } catch (err) {
            console.warn("Error al verificar acceso admin/colaborador:", err.message);
            adminDropdown.classList.add("hidden");
        }
    }
    
    function initializeAdminDropdownHover() {
  const adminDropdown = document.getElementById("admin-dropdown");
  if (!adminDropdown) return;

  const dropdownMenu = adminDropdown.querySelector(".dropdown-menu");
  if (!dropdownMenu) return;

  if (adminDropdown._hoverInitialized) return;
  adminDropdown._hoverInitialized = true;

  let timeoutId;

  adminDropdown.addEventListener("mouseenter", () => {
    clearTimeout(timeoutId);
    dropdownMenu.classList.add("show");
  });

  adminDropdown.addEventListener("mouseleave", () => {
    timeoutId = setTimeout(() => {
      dropdownMenu.classList.remove("show");
    }, 300);
  });

  // Evitar que al entrar al dropdown-menu o sus links se cierre el menú
  dropdownMenu.addEventListener("mouseenter", () => {
    clearTimeout(timeoutId);
    dropdownMenu.classList.add("show");
  });

  dropdownMenu.addEventListener("mouseleave", () => {
    timeoutId = setTimeout(() => {
      dropdownMenu.classList.remove("show");
    }, 300);
  });
}



document.getElementById("admin-options").addEventListener("click", e => {
  if(e.target.tagName === "A"){
     e.preventDefault(); // Para controlar la navegación con JS
    window.location.href = e.target.href; // Navegar a la URL del href
  }
});

    accountBtn.addEventListener('click', async (e) => {
        e.preventDefault();
        const token = await validarToken();
        if (!token) {
            // Si no hay token válido, redirigir al login
            window.location.href = 'usuario/login/login.html';
            return;
        }

        // Si el menú está abierto, lo cerramos
        if (accountMenu.classList.contains('show')) {
            accountMenu.classList.remove('show');
        } else {
            accountMenu.classList.add('show');
            await verificarAccesoAdmin();
        }
    });


    // ✅ Verificación inicial al cargar la página (solo valida, no abre menú)
    (async () => {
        const token = await validarToken();
        if (token) await verificarAccesoAdmin();
    })();
});
