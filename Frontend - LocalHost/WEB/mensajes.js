// Función para mostrar un mensaje global
function mostrarMensaje(text, tipo = "exito") {
    const colorFondo = tipo === "exito" ? "#16a34a" : "#dc2626"; // verde o rojo
    const colorTexto = "#fff";

    const msgDiv = document.createElement("div");
    msgDiv.textContent = text;

    Object.assign(msgDiv.style, {
        position: "fixed",
        top: "10px",
        right: "10px",
        padding: "10px 20px",
        background: colorFondo,
        color: colorTexto,
        borderRadius: "5px",
        boxShadow: "0 2px 6px rgba(0,0,0,0.2)",
        zIndex: 1000,
        fontFamily: "Arial, sans-serif",
        fontSize: "14px",
        opacity: 0,
        transition: "opacity 0.3s ease"
    });

    document.body.appendChild(msgDiv);

    // Animación de entrada
    requestAnimationFrame(() => {
        msgDiv.style.opacity = 1;
    });

    // Desaparece después de 3 segundos
    setTimeout(() => {
        msgDiv.style.opacity = 0;
        msgDiv.addEventListener("transitionend", () => msgDiv.remove());
    }, 3000);
}
// Confirmación atractiva estilo mensaje
function mostrarConfirmacion(text, callback) {
    const overlay = document.createElement("div");
    Object.assign(overlay.style, {
        position: "fixed",
        top: 0,
        left: 0,
        width: "100%",
        height: "100%",
        background: "rgba(0,0,0,0.5)",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        zIndex: 1001
    });

    const modal = document.createElement("div");
    Object.assign(modal.style, {
        background: "#fff",
        padding: "20px 30px",
        borderRadius: "8px",
        textAlign: "center",
        maxWidth: "400px",
        boxShadow: "0 2px 10px rgba(0,0,0,0.2)"
    });

    const mensaje = document.createElement("p");
    mensaje.textContent = text;
    mensaje.style.marginBottom = "20px";

    const btnConfirmar = document.createElement("button");
    btnConfirmar.textContent = "Sí";
    Object.assign(btnConfirmar.style, {
        marginRight: "10px",
        padding: "8px 16px",
        background: "#16a34a",
        color: "#fff",
        border: "none",
        borderRadius: "4px",
        cursor: "pointer"
    });

    const btnCancelar = document.createElement("button");
    btnCancelar.textContent = "No";
    Object.assign(btnCancelar.style, {
        padding: "8px 16px",
        background: "#dc2626",
        color: "#fff",
        border: "none",
        borderRadius: "4px",
        cursor: "pointer"
    });

    btnConfirmar.addEventListener("click", () => {
        document.body.removeChild(overlay);
        callback(true);
    });

    btnCancelar.addEventListener("click", () => {
        document.body.removeChild(overlay);
        callback(false);
    });

    modal.appendChild(mensaje);
    modal.appendChild(btnConfirmar);
    modal.appendChild(btnCancelar);
    overlay.appendChild(modal);
    document.body.appendChild(overlay);
}

// Funciones específicas para éxito y error
function mostrarExito(text) {
    mostrarMensaje(text, "exito");
}

function mostrarError(text) {
    mostrarMensaje(text, "error");
}
