package com.formaprogramada.ecommerce_backend.Domain.Service.MercadoPago;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formaprogramada.ecommerce_backend.Domain.Service.Pedido.PedidoService;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Usuario.UsuarioEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Usuario.JpaUsuarioRepository;
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

            // 2️⃣ Detectar ambiente automáticamente
            boolean isTestToken = mercadolibreToken.startsWith("TEST-");
            boolean isProductionToken = mercadolibreToken.startsWith("APP_USR-");

            if (!isTestToken && !isProductionToken) {
                throw new IllegalArgumentException("Token inválido. Debe comenzar con TEST- o APP_USR-");
            }

            // 3️⃣ Configurar MercadoPago
            MercadoPagoConfig.setAccessToken(mercadolibreToken);

            String ambiente = isTestToken ? "SANDBOX" : "PRODUCCIÓN";
            System.out.println("🔑 Token configurado para: " + ambiente);
            System.out.println("📦 Procesando pedido: " + pedidoId + " por $" + price);

            // 4️⃣ Crear item
            PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                    .id(pedidoId)
                    .title(title)
                    .quantity(quantity)
                    .unitPrice(price)
                    .currencyId("ARS")
                    .categoryId("others") // Categoría general
                    .build();

            // 5️⃣ URLs de retorno
            String successUrl = baseUrl + "/api/mp/pago-exitoso?pedidoId=" + pedidoId;
            String pendingUrl = baseUrl + "/api/mp/pago-pendiente?pedidoId=" + pedidoId;
            String failureUrl = baseUrl + "/api/mp/pago-fallido?pedidoId=" + pedidoId;
            String webhookUrl = baseUrl + "/api/mp/webhook";

            // 6️⃣ Configuración según el ambiente
            PreferenceRequest.PreferenceRequestBuilder requestBuilder = PreferenceRequest.builder()
                    .items(List.of(itemRequest))

                    // URLs de retorno
                    .backUrls(
                            PreferenceBackUrlsRequest.builder()
                                    .success(successUrl)
                                    .pending(pendingUrl)
                                    .failure(failureUrl)
                                    .build()
                    )

                    // Referencias para tracking
                    .externalReference(pedidoId)
                    .notificationUrl(webhookUrl)

                    // Expiración
                    .expires(true)
                    .expirationDateFrom(OffsetDateTime.now())
                    .expirationDateTo(OffsetDateTime.now().plusHours(24))

                    // Descriptor en el resumen de la tarjeta
                    .statementDescriptor("PEDIDO " + pedidoId);

            // 🔥 CONFIGURACIÓN ESPECÍFICA SEGÚN AMBIENTE
            if (isTestToken) {
                // CONFIGURACIÓN SANDBOX
                requestBuilder
                        .binaryMode(false) // Permitir estados intermedios
                        .autoReturn("approved")
                        .paymentMethods(
                                PreferencePaymentMethodsRequest.builder()
                                        .excludedPaymentTypes(List.of()) // No excluir nada en test
                                        .excludedPaymentMethods(List.of())
                                        .installments(12) // Hasta 12 cuotas en test
                                        .defaultInstallments(1)
                                        .build()
                        );
            } else {
                // 🏭 CONFIGURACIÓN PRODUCCIÓN
                requestBuilder
                        .binaryMode(true) // Solo approved/rejected en producción
                        .autoReturn("approved")
                        .paymentMethods(
                                PreferencePaymentMethodsRequest.builder()
                                        // Métodos excluidos (opcional)
                                        .excludedPaymentTypes(List.of(
                                                // "ticket", // Excluir efectivo si no quieres
                                                // "atm" // Excluir cajeros si no quieres
                                        ))
                                        .excludedPaymentMethods(List.of(
                                                // "rapipago", // Excluir Rapipago
                                                // "pagofacil" // Excluir Pago Fácil
                                        ))
                                        .installments(12) // Máximo cuotas permitidas
                                        .defaultInstallments(1) // Cuotas por defecto
                                        .build()
                        )

                        // 🔒 CONFIGURACIÓN ADICIONAL DE SEGURIDAD PARA PRODUCCIÓN
                        .additionalInfo("Compra en tienda online")
                        .marketplace("NONE") // No es marketplace

                        // 📧 Información del comprador (recomendado en producción)
                        .payer(
                                PreferencePayerRequest.builder()
                                        .name("Cliente")
                                        .surname("Tienda")
                                        .email("cliente@email.com") // Email genérico
                                        .build()
                        );
            }

            // 7️⃣ Crear la preferencia
            PreferenceRequest preferenceRequest = requestBuilder.build();
            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            // 8️⃣ Validar que se creó correctamente
            if (preference.getId() == null || preference.getId().isEmpty()) {
                throw new RuntimeException("❌ Preference creada sin ID válido");
            }

            // 9️⃣ Obtener URL según ambiente
            String initPoint;
            if (isTestToken) {
                // SANDBOX: Usar sandbox init point
                initPoint = preference.getSandboxInitPoint();
                if (initPoint == null || initPoint.isEmpty()) {
                    initPoint = "https://sandbox.mercadopago.com.ar/checkout/v1/redirect?pref_id=" + preference.getId();
                }
            } else {
                // PRODUCCIÓN: Usar init point normal
                initPoint = preference.getInitPoint();
                if (initPoint == null || initPoint.isEmpty()) {
                    throw new RuntimeException("❌ No se pudo obtener URL de pago de producción");
                }
            }

            // 🔟 Verificación final según ambiente
            if (isTestToken && !initPoint.contains("sandbox")) {
                throw new RuntimeException("❌ ERROR: Token TEST pero URL no es de sandbox");
            }
            if (isProductionToken && initPoint.contains("sandbox")) {
                throw new RuntimeException("❌ ERROR: Token PRODUCCIÓN pero URL es de sandbox");
            }

            // 1️⃣1️⃣ Guardar preference ID en base de datos
            try {
                pedidoService.guardarMercadoPagoId(pedidoId, preference.getId());
                System.out.println("✅ Preference ID guardado: " + preference.getId());
            } catch (Exception e) {
                System.err.println("⚠️ Error guardando preference ID: " + e.getMessage());
            }

            // 1️⃣2️⃣ Logs de éxito
            System.out.println("✅ Preferencia creada exitosamente en " + ambiente);
            System.out.println("🆔 Preference ID: " + preference.getId());
            System.out.println("🔗 " + ambiente + " URL: " + initPoint);
            System.out.println("🎯 External Reference: " + pedidoId);

            if (isTestToken) {
                System.out.println("💳 Usar tarjetas de TEST de MercadoPago");
            } else {
                System.out.println("💰 PRODUCCIÓN: Procesará pagos REALES");
            }

            return initPoint;

        } catch (MPApiException e) {
            System.err.println("❌ ERROR API MercadoPago:");
            System.err.println("   📊 Status Code: " + e.getStatusCode());
            System.err.println("   💬 Mensaje: " + e.getMessage());

            if (e.getApiResponse() != null) {
                System.err.println("   📄 Respuesta: " + e.getApiResponse().getContent());
            }

            // Sugerencias según el error
            switch (e.getStatusCode()) {
                case 400:
                    System.err.println("💡 Verificar: Formato de datos, URLs HTTPS válidas");
                    break;
                case 401:
                    System.err.println("🔐 Token inválido o expirado");
                    break;
                case 403:
                    System.err.println("🚫 Token sin permisos para crear preferencias");
                    break;
                case 429:
                    System.err.println("⏱️ Rate limit excedido, reintentar en unos minutos");
                    break;
                default:
                    System.err.println("🔍 Error no documentado, revisar con soporte MP");
            }

            throw new RuntimeException("Error API MercadoPago: " + e.getMessage(), e);

        } catch (Exception e) {
            System.err.println("❌ ERROR INESPERADO: " + e.getMessage());
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