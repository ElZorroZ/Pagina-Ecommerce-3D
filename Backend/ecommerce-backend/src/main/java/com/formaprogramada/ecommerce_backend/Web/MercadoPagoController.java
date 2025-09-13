package com.formaprogramada.ecommerce_backend.Web;

import com.formaprogramada.ecommerce_backend.Domain.Model.Pedido.Pedido;
import com.formaprogramada.ecommerce_backend.Domain.Service.MercadoPago.MercadoPagoService;

import com.formaprogramada.ecommerce_backend.Domain.Service.Pedido.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
@RestController
@RequestMapping("/api/mp")
public class MercadoPagoController {

    private final MercadoPagoService mercadoPagoService;

    @Autowired
    private PedidoService pedidoService;

    @Value("${mercadopago.access-token}")
    private String mercadolibreToken;

    public MercadoPagoController(MercadoPagoService mercadoPagoService) {
        this.mercadoPagoService = mercadoPagoService;
    }

    // Crear preferencia y devolver init_point
    @PutMapping("/confirmarPedido")
    public ResponseEntity<Map<String, String>> confirmarPedido(@RequestBody Pedido pedido, @RequestParam int quantity) {
        if (pedido == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Pedido inválido"));
        }

        String title = pedido.getId() + "-" + pedido.getFechaPedido();
        BigDecimal price = BigDecimal.valueOf(pedido.getTotal());
        String initPoint = mercadoPagoService.confirmarPedido(mercadolibreToken, title, price, pedido.getId().toString(), quantity);

<<<<<<< HEAD
            // Parseo JSON
            if (body != null && !body.isEmpty()) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode rootNode = mapper.readTree(body);

                    if (rootNode.has("type") && "payment".equals(rootNode.get("type").asText())) {
                        if (rootNode.has("data") && rootNode.get("data").has("id")) {
                            paymentId = rootNode.get("data").get("id").asText();
                            System.out.println("💳 Payment ID extraído del JSON: " + paymentId);
                        }
                    }
                } catch (Exception jsonEx) {
                    System.err.println("❌ Error parseando JSON: " + jsonEx.getMessage());
                }
            }

            // Fallback
            if (paymentId == null && id != null) {
                paymentId = id;
                System.out.println("💳 Usando Payment ID de parámetro: " + paymentId);
            }

            if (paymentId == null || paymentId.isEmpty()) {
                System.err.println("❌ No se pudo extraer Payment ID");
                return ResponseEntity.ok("NO_PAYMENT_ID");
            }

            // 🔑 Configurar token
            if (mercadolibreToken == null || mercadolibreToken.isEmpty()) {
                System.err.println("❌ Token de MercadoPago no configurado");
                return ResponseEntity.ok("NO_TOKEN");
            }
            MercadoPagoConfig.setAccessToken(mercadolibreToken);

            // 👉 Consultar el pago
            PaymentClient paymentClient = new PaymentClient();
            Payment payment = paymentClient.get(Long.valueOf(paymentId));

            System.out.println("💰 Estado real del pago: " + payment.getStatus());
            System.out.println("📅 Fecha: " + payment.getDateApproved());
            System.out.println("🔗 Pedido asociado (externalReference): " + payment.getExternalReference());

            // 🔄 Mapear estado de MercadoPago a estado interno
            String estadoInterno = mapearEstadoPago(payment.getStatus());
            System.out.println("🔄 Estado mapeado: " + estadoInterno);

            // Actualizar estado en DB
            pedidoService.CambiarEstado(
                    estadoInterno,                        // estado mapeado
                    Integer.parseInt(payment.getExternalReference()) // id convertido a int
            );

        } catch (Exception e) {
            System.err.println("❌ ERROR EN WEBHOOK: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok("ERROR_HANDLED");
        }
        return ResponseEntity.ok("PROCESSED");
    }

    // 🔥 MÉTODO AUXILIAR - Mapear estados de MercadoPago a nuestros estados
    private String mapearEstadoPago(String estadoMercadoPago) {
        switch (estadoMercadoPago) {
            case "approved":
                return "PAGADO";
            case "pending":
            case "in_process":
                return "PENDIENTE";
            case "rejected":
            case "cancelled":
                return "CANCELADO";
            default:
                return "PROCESANDO";
        }
    }


    // 🔥 ENDPOINTS DE REDIRECCIÓN MEJORADOS
=======
        return ResponseEntity.ok(Map.of("initPoint", initPoint));
    }

    // URLs para redirección después del pago
>>>>>>> parent of 391f6a9 (Merge branch 'main' of https://github.com/ElZorroZ/Pagina-Ecommerce-3D)
    @GetMapping("/pago-exitoso")
    public String pagoExitoso(@RequestParam int pedidoId) {
        pedidoService.CambiarEstado("PAGADO", pedidoId);
        return "redirect:/gracias-por-tu-compra";
    }

    @GetMapping("/pago-pendiente")
    public String pagoPendiente(@RequestParam int pedidoId) {
        pedidoService.CambiarEstado("PENDIENTE", pedidoId);
        return "redirect:/estado-pendiente";
    }

    @GetMapping("/pago-fallido")
    public String pagoFallido(@RequestParam int pedidoId) {
        pedidoService.CambiarEstado("FALLIDO", pedidoId);
        return "redirect:/pago-fallido";
    }

    // Webhook para notificaciones automáticas de Mercado Pago
    @PostMapping("/webhook")
    public ResponseEntity<Void> mpWebhook(@RequestBody Map<String, Object> payload) {
        try {
            String type = (String) payload.get("type");
            if ("payment".equals(type)) {
                Map<String, Object> data = (Map<String, Object>) payload.get("data");

                // ID del pago en Mercado Pago como String
                String mercadoPagoId = String.valueOf(data.get("id"));

                // Buscamos el pedido usando mercadoPagoId
                Pedido pedido = pedidoService.obtenerPedidoPorMercadoPagoId(mercadoPagoId);
                if (pedido != null) {
                    Map<String, Object> payment = (Map<String, Object>) data.get("object");
                    String status = (String) payment.get("status"); // approved, pending, rejected, etc.
                    pedidoService.CambiarEstado(status.toUpperCase(), pedido.getId());
                }
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
