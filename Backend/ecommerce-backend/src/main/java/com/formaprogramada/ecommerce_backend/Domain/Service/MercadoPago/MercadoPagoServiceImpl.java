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

            // 2️⃣ Configurar token
            MercadoPagoConfig.setAccessToken(mercadolibreToken);
            boolean isTestToken = mercadolibreToken.startsWith("TEST-");

            System.out.println("🔑 Token configurado: " + (isTestToken ? "SANDBOX" : "PRODUCTION"));
            System.out.println("⚠️ SDK bug: URLs pueden ser de producción pero funcionan con tokens TEST");
            // 3️⃣ Crear item
            PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                    .title(title)
                    .quantity(quantity)
                    .unitPrice(price)
                    .currencyId("ARS")
                    .id(pedidoId)
                    .build();

            // 4️⃣ URLs dinámicas según ambiente
            String successUrl = baseUrl + "/api/mp/pago-exitoso?pedidoId=" + pedidoId;
            String pendingUrl = baseUrl + "/api/mp/pago-pendiente?pedidoId=" + pedidoId;
            String failureUrl = baseUrl + "/api/mp/pago-fallido?pedidoId=" + pedidoId;
            String webhookUrl = baseUrl + "/api/mp/webhook";

            // 5️⃣ Crear preferencia con configuración específica para sandbox
            PreferenceRequest.PreferenceRequestBuilder requestBuilder = PreferenceRequest.builder()
                    .items(List.of(itemRequest))
                    .backUrls(
                            PreferenceBackUrlsRequest.builder()
                                    .success(successUrl)
                                    .pending(pendingUrl)
                                    .failure(failureUrl)
                                    .build()
                    )
                    .externalReference(pedidoId) // 🔥 CLAVE: Para identificar en webhook
                    .notificationUrl(webhookUrl) // 🔥 Webhook URL
                    .expires(true)
                    .expirationDateFrom(OffsetDateTime.now())
                    .expirationDateTo(OffsetDateTime.now().plusHours(24))
                    .statementDescriptor("Pedido #" + pedidoId);

            // 🔥 CONFIGURACIÓN ESPECÍFICA SEGÚN AMBIENTE
            if (isTestToken) {
                // Para sandbox/test
                requestBuilder
                        .binaryMode(false) // Permitir más estados en sandbox
                        .paymentMethods(
                                PreferencePaymentMethodsRequest.builder()
                                        .excludedPaymentTypes(List.of()) // No excluir nada en sandbox
                                        .excludedPaymentMethods(List.of())
                                        .installments(12) // Máximo 12 cuotas
                                        .defaultInstallments(1)
                                        .build()
                        );
            } else {
                // Para producción
                requestBuilder
                        .binaryMode(true) // Solo aprobado/rechazado en producción
                        .paymentMethods(
                                PreferencePaymentMethodsRequest.builder()
                                        .excludedPaymentTypes(List.of())
                                        .excludedPaymentMethods(List.of())
                                        .installments(6)
                                        .defaultInstallments(1)
                                        .build()
                        );
            }


            // 6️⃣ Crear la preferencia
            // En confirmarPedido(), después de crear la preferencia
            PreferenceRequest preferenceRequest = requestBuilder.build();
            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            // 🔥 VALIDAR QUE LA PREFERENCIA SE CREÓ CORRECTAMENTE
            if (preference.getId() == null || preference.getId().isEmpty()) {
                throw new RuntimeException("Error: Preference creada sin ID válido");
            }

            if (preference.getInitPoint() == null || preference.getInitPoint().isEmpty()) {
                throw new RuntimeException("Error: Preference creada sin init_point válido");
            }

            // 🔥 VALIDAR URLs DE WEBHOOK
                        System.out.println("🔍 Validando configuración de webhook:");
                        System.out.println("   - Notification URL: " + webhookUrl);
                        System.out.println("   - External Reference: " + pedidoId);

            // Verificar que el webhook URL sea alcanzable (opcional)
            if (!webhookUrl.startsWith("https://") && !isTestToken) {
                System.err.println("⚠️ CRÍTICO: Webhook debe usar HTTPS en producción");
            }

            // 7️⃣ Guardar preference ID
            try {
                pedidoService.guardarMercadoPagoId(pedidoId, preference.getId());
                System.out.println("✅ Preference ID guardado: " + preference.getId());
            } catch (Exception e) {
                System.err.println("⚠️ Error guardando preference ID: " + e.getMessage());
            }

            // 8️⃣ Log detallado del resultado
            String initPoint = preference.getInitPoint();
            System.out.println("✅ Preferencia creada: " + preference.getId());
            System.out.println("🔗 URL generada: " + initPoint);
            System.out.println("🌐 Tipo de token: " + (isTestToken ? "TEST" : "PRODUCTION"));
            System.out.println("🏛️ Ambiente detectado: " + (initPoint.contains("sandbox") ? "SANDBOX" : "PRODUCTION"));

            // 🔥 VALIDACIÓN FINAL
            if (isTestToken && !initPoint.contains("sandbox")) {
                System.err.println("⚠️ WARNING: Token TEST pero URL de producción generada");
                System.err.println("💡 Esto puede causar problemas en el webhook");
            }

            return initPoint;

        } catch (MPApiException e) {
            System.err.println("❌ Error de API Mercado Pago:");
            System.err.println("   - Status Code: " + e.getStatusCode());
            System.err.println("   - Mensaje: " + e.getMessage());
            System.err.println("   - Respuesta: " + e.getApiResponse());

            if (e.getStatusCode() == 400) {
                System.err.println("💡 Posibles causas:");
                System.err.println("   - Token inválido para el ambiente");
                System.err.println("   - Configuración de preferencia incorrecta");
                System.err.println("   - URLs webhook malformadas");
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