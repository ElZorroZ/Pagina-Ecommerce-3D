package com.formaprogramada.ecommerce_backend.Domain.Service.MercadoPago;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formaprogramada.ecommerce_backend.Domain.Service.Pedido.PedidoService;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.mercadopago.MercadoPagoConfig;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
@Service
public class MercadoPagoServiceImpl implements MercadoPagoService {

    @Autowired
    private PedidoService pedidoService;

    @Value("${mercadopago.webhook.key}")
    private String webhookKey;

    @Value("${mercadopago.sandbox.enabled:true}")
    private boolean sandboxEnabled;

    @Value("${mercadopago.base-url:https://forma-programada.onrender.com}")
    private String baseUrl;

    @Override
    public String confirmarPedido(String mercadolibreToken, String title, BigDecimal price, String pedidoId, int quantity) {
        try {
            // 1️⃣ Validaciones básicas
            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("El precio debe ser mayor a 0");
            }
            if (quantity <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
            }
            if (pedidoId == null || pedidoId.isEmpty()) {
                throw new IllegalArgumentException("El pedidoId no puede ser nulo o vacío");
            }

            // 2️⃣ Verificar que el token sea TEST
            boolean isTestToken = mercadolibreToken.startsWith("TEST-");
            if (!isTestToken) {
                throw new IllegalArgumentException("En desarrollo solo se permiten tokens TEST");
            }

            // 3️⃣ Configurar MercadoPago para sandbox
            MercadoPagoConfig.setAccessToken(mercadolibreToken);

            // 🔥 FORZAR SANDBOX MODE
            System.setProperty("ENVIRONMENT", "sandbox");

            System.out.println("🔑 Token configurado: SANDBOX");
            System.out.println("🌐 Forzando modo sandbox");

            // 4️⃣ Crear item con configuración específica para sandbox
            PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                    .title(title)
                    .quantity(quantity)
                    .unitPrice(price)
                    .currencyId("ARS")
                    .id(pedidoId)
                    .categoryId("others") // 🔥 IMPORTANTE: Categoría para sandbox
                    .build();

            // 5️⃣ URLs con dominio de test
            String successUrl = baseUrl + "/api/mp/pago-exitoso?pedidoId=" + pedidoId;
            String pendingUrl = baseUrl + "/api/mp/pago-pendiente?pedidoId=" + pedidoId;
            String failureUrl = baseUrl + "/api/mp/pago-fallido?pedidoId=" + pedidoId;
            String webhookUrl = baseUrl + "/api/mp/webhook";

            System.out.println("🔍 URLs configuradas:");
            System.out.println("   - Success: " + successUrl);
            System.out.println("   - Webhook: " + webhookUrl);

            // 6️⃣ Configuración específica para sandbox
            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(List.of(itemRequest))
                    .backUrls(
                            PreferenceBackUrlsRequest.builder()
                                    .success(successUrl)
                                    .pending(pendingUrl)
                                    .failure(failureUrl)
                                    .build()
                    )
                    .externalReference(pedidoId)
                    .notificationUrl(webhookUrl)

                    // 🔥 CONFIGURACIÓN CRÍTICA PARA SANDBOX
                    .binaryMode(false) // IMPORTANTE: false para sandbox
                    .autoReturn("approved") // Auto retorno en aprobados

                    // Configuración de pagos para sandbox
                    .paymentMethods(
                            PreferencePaymentMethodsRequest.builder()
                                    .excludedPaymentTypes(List.of()) // No excluir nada
                                    .excludedPaymentMethods(List.of()) // No excluir métodos
                                    .installments(12) // Hasta 12 cuotas
                                    .defaultInstallments(1)
                                    .defaultPaymentMethodId("visa") // Método por defecto
                                    .build()
                    )

                    // Información del pagador para sandbox (opcional pero recomendado)
                    .payer(
                            PreferencePayerRequest.builder()
                                    .name("Test")
                                    .surname("User")
                                    .email("test_user_123456@testuser.com") // Email de test válido
                                    .build()
                    )

                    // Configuración de expiración
                    .expires(true)
                    .expirationDateFrom(OffsetDateTime.now())
                    .expirationDateTo(OffsetDateTime.now().plusHours(24))
                    .statementDescriptor("Test Pedido #" + pedidoId)

                    // 🔥 METADATOS PARA IDENTIFICAR SANDBOX
                    .metadata(Map.of(
                            "environment", "sandbox",
                            "pedido_id", pedidoId,
                            "integration_test", "true"
                    ))

                    .build();

            // 7️⃣ Crear la preferencia con cliente configurado para sandbox
            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            // 8️⃣ Validaciones post-creación
            if (preference.getId() == null || preference.getId().isEmpty()) {
                throw new RuntimeException("Error: Preference creada sin ID válido");
            }

            // 9️⃣ OBTENER URL DE SANDBOX CORRECTAMENTE
            String initPoint = preference.getSandboxInitPoint();

            // 🔥 FALLBACK si getSandboxInitPoint es null
            if (initPoint == null || initPoint.isEmpty()) {
                initPoint = preference.getInitPoint();
                System.out.println("⚠️ Usando initPoint estándar como fallback");
            }

            // 🔥 VALIDACIÓN CRÍTICA: Asegurar que sea URL de sandbox
            if (!initPoint.contains("sandbox")) {
                // Forzar URL de sandbox si no la detecta
                String preferenceId = preference.getId();
                initPoint = "https://sandbox.mercadopago.com.ar/checkout/v1/redirect?pref_id=" + preferenceId;
                System.out.println("🔧 URL de sandbox forzada: " + initPoint);
            }

            // 🔟 Guardar preference ID
            try {
                pedidoService.guardarMercadoPagoId(pedidoId, preference.getId());
                System.out.println("✅ Preference ID guardado: " + preference.getId());
            } catch (Exception e) {
                System.err.println("⚠️ Error guardando preference ID: " + e.getMessage());
            }

            // 1️⃣1️⃣ Log detallado del resultado
            System.out.println("✅ Preferencia creada: " + preference.getId());
            System.out.println("🔗 URL generada: " + initPoint);
            System.out.println("🏛️ Ambiente confirmado: SANDBOX");
            System.out.println("💳 Usar tarjetas de test: https://www.mercadopago.com.ar/developers/es/docs/testing/test-cards");

            return initPoint;

        } catch (MPApiException e) {
            System.err.println("❌ Error de API Mercado Pago:");
            System.err.println("   - Status Code: " + e.getStatusCode());
            System.err.println("   - Mensaje: " + e.getMessage());
            System.err.println("   - Respuesta: " + e.getApiResponse());

            // Mensajes específicos para errores comunes en sandbox
            if (e.getStatusCode() == 400) {
                System.err.println("💡 Verificar:");
                System.err.println("   - Token TEST válido y activo");
                System.err.println("   - Cuenta de test creada correctamente");
                System.err.println("   - URLs webhook accesibles desde internet");
            } else if (e.getStatusCode() == 401) {
                System.err.println("🔐 Token inválido o expirado");
            }

            throw new RuntimeException("Error creando preferencia: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("❌ Error inesperado: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error procesando pago: " + e.getMessage(), e);
        }
    }
    public boolean verifySignature(String signature, Map<String, Object> payload) {
        System.out.println("🔐 Verificando webhook signature:");
        System.out.println("   - Signature recibida: " + (signature != null ? "SÍ" : "NO"));
        System.out.println("   - Webhook key configurada: " + (webhookKey != null ? "SÍ" : "NO"));
        System.out.println("   - Modo sandbox: " + sandboxEnabled);

        // 🔥 EN SANDBOX, SER MÁS PERMISIVO CON LA VERIFICACIÓN
        if (sandboxEnabled) {
            System.out.println("🧪 Modo sandbox: verificación permisiva activada");

            // Si no hay signature en sandbox, permitir
            if (signature == null) {
                System.out.println("⚠️ Sin signature en sandbox - PERMITIENDO");
                return true;
            }

            // Si no hay webhook key configurada en sandbox, permitir
            if (webhookKey == null || webhookKey.trim().isEmpty()) {
                System.out.println("⚠️ Sin webhook key en sandbox - PERMITIENDO");
                return true;
            }
        }

        // Verificación normal para producción o cuando hay signature
        if (signature == null || webhookKey == null) {
            System.out.println("❌ Signature o webhook key faltante en producción");
            return false;
        }

        try {
            String payloadString = new ObjectMapper().writeValueAsString(payload);
            SecretKeySpec secretKey = new SecretKeySpec(webhookKey.getBytes(), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(payloadString.getBytes());
            String computedSignature = "sha256=" + Base64.getEncoder().encodeToString(hash);

            System.out.println("🔐 Signature esperada: " + computedSignature);
            System.out.println("🔐 Signature recibida: " + signature);

            boolean isValid = computedSignature.equals(signature);
            System.out.println("🔐 Signature válida: " + isValid);

            return isValid;

        } catch (Exception e) {
            System.err.println("❌ Error verificando signature: " + e.getMessage());
            e.printStackTrace();

            // En sandbox, si hay error de verificación, permitir
            if (sandboxEnabled) {
                System.out.println("🧪 Error en sandbox - PERMITIENDO por desarrollo");
                return true;
            }

            return false;
        }
    }
}