// api.js
// api.js
const API_BASE_URL = "https://forma-programada.onrender.com/api/mp"; // backend en Render

async function verificarPago(pedidoId) {
    try {
        const response = await fetch(`${API_BASE_URL}/verificar-pago/${pedidoId}`, {
            method: "GET",
            headers: {
                "Accept": "application/json"
            }
        });

        if (!response.ok) {
            let errorMsg = response.statusText;
            try {
                const errorData = await response.json();
                if (errorData && errorData.error) errorMsg = errorData.error;
            } catch {}
            throw new Error(`Error al verificar pago: ${errorMsg}`);
        }

        return await response.json();
    } catch (error) {
        console.error("Error en verificarPago API:", error);
        throw error;
    }
}

// Ejecutar apenas carga la página
document.addEventListener("DOMContentLoaded", async () => {
    try {
        // Pedir al usuario que ingrese el ID del pedido
        const pedidoId = prompt("Ingrese el ID del pedido que desea verificar:");

        if (!pedidoId) {
            console.warn("No se ingresó ningún ID");
            return;
        }

        const pago = await verificarPago(pedidoId);
        console.log("✅ Estado del pago:", pago);

        const infoDiv = document.getElementById("mp-config");
        if (pago) {
            infoDiv.innerText = JSON.stringify(pago, null, 2);
        } else {
            infoDiv.innerText = "❌ No se pudo obtener la información del pago.";
        }
    } catch (error) {
        console.error("Error al cargar info de pago:", error);
        document.getElementById("mp-config").innerText = "❌ Error al consultar el pago.";
    }
});
