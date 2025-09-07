package com.formaprogramada.ecommerce_backend.Domain.Service.MercadoPago;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
@Service
public class MercadoPagoServiceImpl implements MercadoPagoService {
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

            // 3️⃣ Crear item
            PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                    .title(title)
                    .quantity(quantity)
                    .unitPrice(price)
                    .currencyId("ARS")
                    .id(pedidoId)
                    .build();

            // 4️⃣ Crear preferencia
            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(List.of(itemRequest))
                    .backUrls(
                            PreferenceBackUrlsRequest.builder()
                                    .success("https://forma-programada.onrender.com/api/mp/pago-exitoso?pedidoId=" + pedidoId)
                                    .pending("https://forma-programada.onrender.com/api/mp/pago-pendiente?pedidoId=" + pedidoId)
                                    .failure("https://forma-programada.onrender.com/api/mp/pago-fallido?pedidoId=" + pedidoId)
                                    .build()
                    )
                    .autoReturn("approved") // Redirige automáticamente cuando se aprueba
                    .build();

            // 5️⃣ Crear la preferencia en Mercado Pago
            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            System.out.println("✅ Preferencia creada: " + preference.getId());
            return preference.getInitPoint();

        } catch (MPApiException e) {
            // Log completo para debug
            System.err.println("Error de API Mercado Pago: " + e.getApiResponse());
            throw new RuntimeException("Error creando preferencia en Mercado Pago", e);
        } catch (MPException e) {
            e.printStackTrace();
            throw new RuntimeException("Error general de Mercado Pago", e);
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }

}
