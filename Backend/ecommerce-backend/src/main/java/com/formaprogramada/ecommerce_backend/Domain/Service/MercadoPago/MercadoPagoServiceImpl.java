package com.formaprogramada.ecommerce_backend.Domain.Service.MercadoPago;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MercadoPagoServiceImpl implements MercadoPagoService{
    @Override
    public String confirmarPedido(String mercadolibreToken, String title, BigDecimal price, String id, int quantity) {
        try{

            MercadoPagoConfig.setAccessToken(mercadolibreToken);
            PreferenceItemRequest itemRequest= PreferenceItemRequest.builder()
                    .title(title)
                    .quantity(quantity)
                    .unitPrice(price)
                    .currencyId("ARS")
                    .id(id)
                    .build();

            List<PreferenceItemRequest> items= new ArrayList<>();
            items.add(itemRequest);
            PreferenceBackUrlsRequest backUrlsRequest= PreferenceBackUrlsRequest.builder().
                    success("https://youtube.com")
                    .pending("https://youtube.com")
                    .failure("https://youtube.com")
                    .build();

            PreferenceRequest preferenceRequest= PreferenceRequest.builder()
                    .items(items)
                    .backUrls(backUrlsRequest)
                    .build();

            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            return preference.getId();

        } catch (RuntimeException | MPException | MPApiException e) {
            System.out.println("Status: " + e.getMessage());
            System.out.println("Response: " + e.getMessage());

            throw new RuntimeException(e);
        }
    }
}
