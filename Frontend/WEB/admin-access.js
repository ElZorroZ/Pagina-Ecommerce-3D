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

    // --- Hover account ---
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

    // --- Logout ---
    if (logoutBtn) {
        logoutBtn.addEventListener('click', (e) => {
            e.preventDefault();
            authManager.logout();
        });
    }

    // --- Verificar roles y mostrar opciones ---
    async function verificarAccesoAdmin() {
        const token = authManager.getAccessToken();
        if (!token || !adminDropdown || !adminOptions) return;

        try {
            const payloadBase64 = token.split('.')[1];
            const payload = JSON.parse(atob(payloadBase64));
            const roles = payload.roles || [];

            if (roles.includes("ROLE_ADMIN") || roles.includes("ROLE_COLABORADOR")) {
                adminDropdown.classList.remove("hidden");

                adminOptions.innerHTML = "";

                if (roles.includes("ROLE_ADMIN")) {
                    adminOptions.innerHTML = `
                        <a href="/admin/CRUDCategoria/CRUDcategoria.html" class="dropdown-category">Gestionar categorías</a>
                        <a href="/admin/CRUDProducto/CRUDproducto.html" class="dropdown-category">Gestionar productos</a>
                        <a href="/admin/Pedidos/pedidos.html" class="dropdown-category">Gestionar pedidos</a>
                        <a href="/admin/Aprobacion/CRUDaprobacion.html" class="dropdown-category">Gestionar colaboradores</a>
                        <a href="/admin/AprobacionProducto/CRUDaprobacionProducto.html" class="dropdown-category">Gestionar productos sin aprobar</a>
                    `;
                } else {
                    adminOptions.innerHTML = `
                        <a href="/admin/CRUDColaboradorProducto/CRUDproductoC.html" class="dropdown-category">Gestionar productos</a>
                    `;
                }

                initializeAdminDropdownHover();
            } else {
                adminDropdown.classList.add("hidden");
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
        if (e.target.tagName === "A") {
            e.preventDefault();
            window.location.href = e.target.href;
        }
    });

    // --- Account button ---
    accountBtn.addEventListener('click', async (e) => {
        e.preventDefault();

        if (!authManager.isAuthenticated()) {
            authManager.redirectToLogin();
            return;
        }

        if (accountMenu.classList.contains('show')) {
            accountMenu.classList.remove('show');
        } else {
            accountMenu.classList.add('show');
            await verificarAccesoAdmin();
        }
    });

    // --- Verificación inicial ---
    (async () => {
        if (authManager.isAuthenticated()) {
            await verificarAccesoAdmin();
        }
    })();
});
