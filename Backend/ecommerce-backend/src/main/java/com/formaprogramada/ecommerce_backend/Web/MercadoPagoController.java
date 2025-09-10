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

    @Value("${mercadopago.sandbox.enabled:true}")
    private boolean sandboxEnabled;

    public MercadoPagoController(MercadoPagoService mercadoPagoService) {
        this.mercadoPagoService = mercadoPagoService;
    }



    // 🔥 ENDPOINT PARA OBTENER INFORMACIÓN DE UN PAGO ESPECÍFICO
    @GetMapping("/payment-info/{paymentId}")
    public ResponseEntity<Map<String, Object>> getPaymentInfo(@PathVariable String paymentId) {
        Map<String, Object> info = new HashMap<>();

        try {
            System.out.println("🔍 Obteniendo información del pago: " + paymentId);

            MercadoPagoConfig.setAccessToken(mercadolibreToken);
            PaymentClient paymentClient = new PaymentClient();
            Payment payment = paymentClient.get(Long.valueOf(paymentId));

            info.put("success", true);
            info.put("paymentId", payment.getId());
            info.put("status", payment.getStatus());
            info.put("externalReference", payment.getExternalReference());
            info.put("amount", payment.getTransactionAmount());
            info.put("currencyId", payment.getCurrencyId());
            info.put("dateCreated", payment.getDateCreated());
            info.put("dateApproved", payment.getDateApproved());
            info.put("paymentMethodId", payment.getPaymentMethodId());
            info.put("paymentTypeId", payment.getPaymentTypeId());

            // Información del pedido asociado
            if (payment.getExternalReference() != null) {
                try {
                    Integer pedidoId = Integer.valueOf(payment.getExternalReference());
                    Pedido pedido = pedidoService.obtenerPedidoPorId(pedidoId);

                    if (pedido != null) {
                        info.put("pedidoFound", true);
                        info.put("pedidoId", pedido.getId());
                        info.put("pedidoEstado", pedido.getEstado());
                        info.put("pedidoTotal", pedido.getTotal());
                    } else {
                        info.put("pedidoFound", false);
                        info.put("pedidoError", "Pedido no encontrado con ID: " + pedidoId);
                    }
                } catch (NumberFormatException e) {
                    info.put("pedidoFound", false);
                    info.put("pedidoError", "External reference no es un ID válido: " + payment.getExternalReference());
                }
            }

            return ResponseEntity.ok(info);

        } catch (Exception e) {
            System.err.println("❌ Error obteniendo info del pago: " + e.getMessage());
            info.put("success", false);
            info.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(info);
        }
    }
    @GetMapping("/auto-detect-environment")
    public ResponseEntity<Map<String, Object>> autoDetectEnvironment() {
        Map<String, Object> detection = new HashMap<>();

        try {
            if (mercadolibreToken == null) {
                detection.put("error", "Token no configurado");
                return ResponseEntity.badRequest().body(detection);
            }

            // Crear una preferencia de prueba para detectar el ambiente
            MercadoPagoConfig.setAccessToken(mercadolibreToken);

            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .title("Test Environment Detection")
                    .quantity(1)
                    .unitPrice(new BigDecimal("1.00"))
                    .currencyId("ARS")
                    .build();

            PreferenceRequest request = PreferenceRequest.builder()
                    .items(List.of(item))
                    .externalReference("env-detection-test")
                    .expires(true)
                    .expirationDateFrom(OffsetDateTime.now())
                    .expirationDateTo(OffsetDateTime.now().plusMinutes(1)) // Expira en 1 minuto
                    .build();

            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(request);

            String initPoint = preference.getInitPoint();
            boolean isActualSandbox = initPoint.contains("sandbox.mercadopago");
            boolean isActualProduction = initPoint.contains("www.mercadopago") && !initPoint.contains("sandbox");

            String realEnvironment = isActualSandbox ? "SANDBOX" :
                    isActualProduction ? "PRODUCTION" : "UNKNOWN";

            String tokenType = mercadolibreToken.startsWith("TEST-") ? "TEST" :
                    mercadolibreToken.startsWith("APP_USR-") ? "PRODUCTION" : "UNKNOWN";

            boolean isConsistent = (sandboxEnabled && isActualSandbox) || (!sandboxEnabled && isActualProduction);

            detection.put("currentConfig", sandboxEnabled ? "SANDBOX" : "PRODUCTION");
            detection.put("detectedEnvironment", realEnvironment);
            detection.put("tokenType", tokenType);
            detection.put("isConsistent", isConsistent);
            detection.put("initPointGenerated", initPoint);
            detection.put("preferenceId", preference.getId());

            if (!isConsistent) {
                detection.put("recommendation", Map.of(
                        "issue", "Configuración inconsistente detectada",
                        "solution", realEnvironment.equals("PRODUCTION") ?
                                "Cambiar mercadopago.sandbox.enabled=false en application.properties" :
                                "Cambiar mercadopago.sandbox.enabled=true en application.properties",
                        "alternativeSolution", "O cambiar el token por uno que coincida con la configuración actual"
                ));
            } else {
                detection.put("recommendation", "✅ Configuración correcta");
            }

            return ResponseEntity.ok(detection);

        } catch (Exception e) {
            detection.put("error", "Error detectando ambiente: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(detection);
        }
    }
    @GetMapping("/config-status")
    public ResponseEntity<Map<String, Object>> getConfigStatus() {
        Map<String, Object> status = new HashMap<>();

        try {
            // Verificar token
            boolean tokenValid = mercadolibreToken != null && !mercadolibreToken.isEmpty();
            String tokenType = "UNKNOWN";

            if (tokenValid) {
                if (mercadolibreToken.startsWith("TEST-")) {
                    tokenType = "TEST/SANDBOX";
                } else if (mercadolibreToken.startsWith("APP_USR-")) {
                    tokenType = "PRODUCCIÓN";
                }
            }

            status.put("tokenConfigured", tokenValid);
            status.put("tokenType", tokenType);
            status.put("sandboxEnabled", sandboxEnabled);
            status.put("tokenLength", tokenValid ? mercadolibreToken.length() : 0);
            status.put("recommendation", getRecommendation(tokenType, sandboxEnabled));

            return ResponseEntity.ok(status);

        } catch (Exception e) {
            status.put("error", "Error verificando configuración: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(status);
        }
    }

    private String getRecommendation(String tokenType, boolean sandboxEnabled) {
        if ("TEST/SANDBOX".equals(tokenType) && sandboxEnabled) {
            return "✅ Configuración correcta para sandbox";
        } else if ("PRODUCCIÓN".equals(tokenType) && !sandboxEnabled) {
            return "✅ Configuración correcta para producción";
        } else if ("TEST/SANDBOX".equals(tokenType) && !sandboxEnabled) {
            return "⚠️ Token de sandbox pero configurado para producción";
        } else if ("PRODUCCIÓN".equals(tokenType) && sandboxEnabled) {
            return "⚠️ Token de producción pero configurado para sandbox";
        } else {
            return "❌ Configuración inválida";
        }
    }

    // Confirmar pedido y obtener link de pago
    @PutMapping("/confirmarPedido")
    public ResponseEntity<Map<String, String>> confirmarPedido(@RequestBody Pedido pedido, @RequestParam int quantity) {
        try {
            // 🔥 VALIDACIÓN MEJORADA DE CONFIGURACIÓN
            if (mercadolibreToken == null || mercadolibreToken.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Token de MercadoPago no configurado"));
            }

            // 🔥 VERIFICAR que el token se configuró correctamente
            System.out.println("🔑 Token configurado: " + (mercadolibreToken.startsWith("TEST-") ? "SANDBOX" : "PRODUCTION"));
            // Validaciones existentes
            if (pedido == null || pedido.getId() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Pedido inválido o sin ID"));
            }
            if (quantity <= 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Cantidad debe ser mayor a 0"));
            }
            if (pedido.getTotal() <= 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Total del pedido debe ser mayor a 0"));
            }

            // Construir título descriptivo
            String title = "Pedido #" + pedido.getId() + " - " + pedido.getFechaPedido();
            BigDecimal price = BigDecimal.valueOf(pedido.getTotal());

            // 🔥 LOG DE CONFIGURACIÓN ANTES DE PROCESAR
            System.out.println("🔧 Procesando pago con configuración:");
            System.out.println("   - Ambiente: " + (sandboxEnabled ? "SANDBOX" : "PRODUCCIÓN"));
            System.out.println("   - Token tipo: " + (mercadolibreToken.startsWith("TEST-") ? "TEST" : "PRODUCCIÓN"));
            System.out.println("   - Pedido ID: " + pedido.getId());
            System.out.println("   - Total: $" + price);

            // Crear preferencia y obtener link
            String initPoint = mercadoPagoService.confirmarPedido(mercadolibreToken, title, price, pedido.getId().toString(), quantity);

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

            // 🔥 ERROR HANDLING MEJORADO
            String errorMessage = "Error procesando pago";
            if (e.getMessage().contains("401")) {
                errorMessage = "Token de acceso inválido. Verifica las credenciales de MercadoPago";
            } else if (e.getMessage().contains("400")) {
                errorMessage = "Datos inválidos. Verifica la configuración del pedido";
            } else if (e.getMessage().contains("credentials")) {
                errorMessage = "Error de credenciales. Verifica el token de MercadoPago";
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", errorMessage,
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

            // 👉 AQUÍ va el bloque que consultará el pago
            PaymentClient paymentClient = new PaymentClient();
            Payment payment = paymentClient.get(Long.valueOf(paymentId));

            System.out.println("💰 Estado real del pago: " + payment.getStatus());
            System.out.println("📅 Fecha: " + payment.getDateApproved());
            System.out.println("🔗 Pedido asociado (externalReference): " + payment.getExternalReference());

            // Actualizar estado en DB
            pedidoService.CambiarEstado(
                    payment.getStatus().toString(),     // estado
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
    @GetMapping("/pago-exitoso")
    public String pagoExitoso(@RequestParam int pedidoId,
                              @RequestParam(required = false) String collection_status,
                              @RequestParam(required = false) Long payment_id) {
        System.out.println("✅ Pago exitoso para pedido: " + pedidoId);
        System.out.println("📋 Status: " + collection_status);
        System.out.println("💳 Payment ID: " + payment_id);

        try {
            // Siempre intentar verificar el pago real si tenemos payment_id
            if (payment_id != null) {
                MercadoPagoConfig.setAccessToken(mercadolibreToken);
                PaymentClient paymentClient = new PaymentClient();
                Payment payment = paymentClient.get(payment_id);

                String estadoReal = mapearEstadoPago(payment.getStatus());
                pedidoService.CambiarEstado(estadoReal, pedidoId);
                System.out.println("✅ Estado actualizado según pago real: " + estadoReal);
            } else if ("approved".equals(collection_status)) {
                // Fallback si no tenemos payment_id pero el status indica aprobado
                pedidoService.CambiarEstado("PAGADO", pedidoId);
                System.out.println("✅ Estado actualizado a PAGADO por collection_status");
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error actualizando estado en redirect: " + e.getMessage());
            // No fallar el redirect por esto
        }

        return "redirect:/gracias-por-tu-compra";
    }

    @GetMapping("/pago-pendiente")
    public String pagoPendiente(@RequestParam int pedidoId,
                                @RequestParam(required = false) Long payment_id) {
        System.out.println("⏳ Pago pendiente para pedido: " + pedidoId);

        try {
            if (payment_id != null) {
                MercadoPagoConfig.setAccessToken(mercadolibreToken);
                PaymentClient paymentClient = new PaymentClient();
                Payment payment = paymentClient.get(payment_id);

                String estadoReal = mapearEstadoPago(payment.getStatus());
                pedidoService.CambiarEstado(estadoReal, pedidoId);
                System.out.println("✅ Estado actualizado según pago real: " + estadoReal);
            } else {
                pedidoService.CambiarEstado("PENDIENTE", pedidoId);
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error actualizando estado: " + e.getMessage());
        }

        return "redirect:/estado-pendiente";
    }

    @GetMapping("/pago-fallido")
    public String pagoFallido(@RequestParam int pedidoId,
                              @RequestParam(required = false) Long payment_id) {
        System.out.println("❌ Pago fallido para pedido: " + pedidoId);

        try {
            if (payment_id != null) {
                MercadoPagoConfig.setAccessToken(mercadolibreToken);
                PaymentClient paymentClient = new PaymentClient();
                Payment payment = paymentClient.get(payment_id);

                String estadoReal = mapearEstadoPago(payment.getStatus());
                pedidoService.CambiarEstado(estadoReal, pedidoId);
                System.out.println("✅ Estado actualizado según pago real: " + estadoReal);
            } else {
                pedidoService.CambiarEstado("CANCELADO", pedidoId);
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error actualizando estado: " + e.getMessage());
        }

        return "redirect:/pago-fallido";
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