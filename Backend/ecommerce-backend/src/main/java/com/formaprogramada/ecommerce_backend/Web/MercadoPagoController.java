package com.formaprogramada.ecommerce_backend.Web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.formaprogramada.ecommerce_backend.Domain.Model.Pedido.Pedido;
import com.formaprogramada.ecommerce_backend.Domain.Service.MercadoPago.MercadoPagoService;

import com.formaprogramada.ecommerce_backend.Domain.Service.Pedido.PedidoService;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.net.MPResourceList;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.resources.payment.Payment;
import java.math.BigDecimal;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/mp")
public class MercadoPagoController {

    private final MercadoPagoService mercadoPagoService;
    @Autowired
    private PedidoService pedidoService;

    @Value("${mercadopago.access-token}")
    private String mercadolibreToken;

    @Value("${mercadopago.base-url}")
    private String baseUrl;
    @Value("${mercadopago.sandbox.enabled:true}")
    private boolean sandboxEnabled;

    public MercadoPagoController(MercadoPagoService mercadoPagoService) {
        this.mercadoPagoService = mercadoPagoService;
    }

    // Confirmar pedido y obtener link de pago
    @PutMapping("/confirmarPedido")
    public ResponseEntity<Map<String, String>> confirmarPedido(@RequestBody Pedido pedido) { // quitar quantity
        try {
            // 🔥 VALIDACIÓN DE TOKEN
            if (mercadolibreToken == null || mercadolibreToken.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Token de MercadoPago no configurado"));
            }

            if (pedido == null || pedido.getId() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Pedido inválido o sin ID"));
            }
            if (pedido.getTotal() <= 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Total del pedido debe ser mayor a 0"));
            }

            // Construir título descriptivo
            String title = "Pedido #" + pedido.getId() + " - " + pedido.getFechaPedido();
            BigDecimal price = BigDecimal.valueOf(pedido.getTotal()); // ya es el total

            // 🔥 LOG
            System.out.println("🔧 Procesando pago con configuración:");
            System.out.println("   - Pedido ID: " + pedido.getId());
            System.out.println("   - Total: $" + price);

            // Crear preferencia y obtener link SIN multiplicar por quantity
            String initPoint = mercadoPagoService.confirmarPedido(mercadolibreToken, title, price, pedido.getId().toString(), 1); // pasar 1

            // Actualizar estado del pedido a "PROCESANDO"
            pedidoService.CambiarEstado("PROCESANDO", pedido.getId());

            System.out.println("✅ Pedido #" + pedido.getId() + " enviado a MercadoPago");
            System.out.println("🔗 Link de pago generado: " + initPoint);

            return ResponseEntity.ok(Map.of(
                    "initPoint", initPoint,
                    "pedidoId", pedido.getId().toString(),
                    "status", "PROCESANDO",
                    "environment", sandboxEnabled ? "sandbox" : "production"
            ));

        } catch (Exception e) {
            System.err.println("❌ Error confirmando pedido: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Error procesando pago",
                            "details", e.getMessage(),
                            "environment", sandboxEnabled ? "sandbox" : "production"
                    ));
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(@RequestBody(required = false) String body,
                                          @RequestParam(required = false) String topic,
                                          @RequestParam(required = false) String id,
                                          HttpServletRequest request) {
        try {
            System.out.println("🔔 WEBHOOK RECIBIDO - " + new Date());
            System.out.println("📋 Body completo: " + body);

            String paymentId = null;

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


    @GetMapping("/pago-exitoso")
    public ResponseEntity<Void> pagoExitoso(@RequestParam int pedidoId,
                                            @RequestParam(required = false) String collection_status,
                                            @RequestParam(required = false) String payment_id) {
        System.out.println("✅ Pago exitoso para pedido: " + pedidoId);
        System.out.println("📋 Status: " + collection_status);
        System.out.println("💳 Payment ID: " + payment_id);

        try {
            Long paymentIdLong = null;
            if (payment_id != null && !"null".equalsIgnoreCase(payment_id)) {
                try {
                    paymentIdLong = Long.valueOf(payment_id);
                } catch (NumberFormatException e) {
                    System.err.println("⚠️ Payment ID inválido: " + payment_id);
                }
            }

            if (paymentIdLong != null) {
                MercadoPagoConfig.setAccessToken(mercadolibreToken);
                PaymentClient paymentClient = new PaymentClient();
                Payment payment = paymentClient.get(paymentIdLong);

                String estadoReal = mapearEstadoPago(payment.getStatus());
                pedidoService.CambiarEstado(estadoReal, pedidoId);
                System.out.println("✅ Estado actualizado según pago real: " + estadoReal);
            } else if ("approved".equals(collection_status)) {
                pedidoService.CambiarEstado("PAGADO", pedidoId);
                System.out.println("✅ Estado actualizado a PAGADO por collection_status");
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error actualizando estado en redirect: " + e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("https://forma-programada.netlify.app/confirmar-pago.html"))
                .build();
    }

    @GetMapping("/pago-pendiente")
    public ResponseEntity<Void> pagoPendiente(@RequestParam int pedidoId,
                                              @RequestParam(required = false) String payment_id) {
        System.out.println("⏳ Pago pendiente para pedido: " + pedidoId);

        try {
            Long paymentIdLong = null;
            if (payment_id != null && !"null".equalsIgnoreCase(payment_id)) {
                try {
                    paymentIdLong = Long.valueOf(payment_id);
                } catch (NumberFormatException e) {
                    System.err.println("⚠️ Payment ID inválido: " + payment_id);
                }
            }

            if (paymentIdLong != null) {
                MercadoPagoConfig.setAccessToken(mercadolibreToken);
                PaymentClient paymentClient = new PaymentClient();
                Payment payment = paymentClient.get(paymentIdLong);

                String estadoReal = mapearEstadoPago(payment.getStatus());
                pedidoService.CambiarEstado(estadoReal, pedidoId);
                System.out.println("✅ Estado actualizado según pago real: " + estadoReal);
            } else {
                pedidoService.CambiarEstado("PENDIENTE", pedidoId);
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error actualizando estado: " + e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("https://forma-programada.netlify.app/pago-pendiente.html"))
                .build();
    }

    @GetMapping("/pago-fallido")
    public ResponseEntity<Void> pagoFallido(@RequestParam int pedidoId,
                                            @RequestParam(required = false) String payment_id) {
        System.out.println("❌ Pago fallido para pedido: " + pedidoId);

        try {
            Long paymentIdLong = null;
            if (payment_id != null && !"null".equalsIgnoreCase(payment_id)) {
                try {
                    paymentIdLong = Long.valueOf(payment_id);
                } catch (NumberFormatException e) {
                    System.err.println("⚠️ Payment ID inválido: " + payment_id);
                }
            }

            if (paymentIdLong != null) {
                MercadoPagoConfig.setAccessToken(mercadolibreToken);
                PaymentClient paymentClient = new PaymentClient();
                Payment payment = paymentClient.get(paymentIdLong);

                String estadoReal = mapearEstadoPago(payment.getStatus());
                pedidoService.CambiarEstado(estadoReal, pedidoId);
                System.out.println("✅ Estado actualizado según pago real: " + estadoReal);
            } else {
                pedidoService.CambiarEstado("CANCELADO", pedidoId);
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error actualizando estado: " + e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("https://forma-programada.netlify.app/pago-fallido.html"))
                .build();
    }


    // 🔥 ENDPOINT PARA VERIFICACIÓN MANUAL DE PAGO
    @GetMapping("/verificar-pago/{pedidoId}")
    public ResponseEntity<Map<String, Object>> verificarPago(@PathVariable Integer pedidoId) {
        try {
            System.out.println("🔍 Verificando pedido: " + pedidoId);

            Pedido pedido = pedidoService.obtenerPedidoPorId(pedidoId);
            String estadoAnterior = pedido.getEstado();

            System.out.println("📋 Estado actual del pedido: " + estadoAnterior);
            System.out.println("🔑 External Payment ID: " + pedido.getExternalPaymentId());

            Map<String, Object> response = new HashMap<>();
            response.put("pedidoId", pedidoId);
            response.put("estadoAnterior", estadoAnterior);

            // Si el pedido ya está PAGADO, no hay nada que verificar
            if ("PAGADO".equals(estadoAnterior)) {
                response.put("estadoActual", estadoAnterior);
                response.put("success", true);
                response.put("mensaje", "Pedido ya está pagado");
                return ResponseEntity.ok(response);
            }

            // Para testing/desarrollo, permitir forzar el estado a PAGADO
            if (sandboxEnabled) {
                System.out.println("🧪 Modo SANDBOX - Forzando estado a PAGADO para testing");
                pedidoService.CambiarEstado("PAGADO", pedidoId);

                response.put("estadoActual", "PAGADO");
                response.put("success", true);
                response.put("mensaje", "Estado forzado a PAGADO (modo sandbox)");
                response.put("sandbox", true);

                return ResponseEntity.ok(response);
            }

            // En producción, mantener estado actual
            response.put("estadoActual", estadoAnterior);
            response.put("success", "PAGADO".equals(estadoAnterior));
            response.put("mensaje", "Use el webhook para actualizaciones automáticas");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Error verificando pago: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", e.getMessage(),
                            "pedidoId", pedidoId
                    ));
        }
    }
}