package com.formaprogramada.ecommerce_backend.Domain.Service.MercadoPago;

import java.math.BigDecimal;
import java.util.Map;

public interface MercadoPagoService {
    String confirmarPedido (String mercadolibreToken, String title, BigDecimal price,String id, int quantity);
    boolean verifySignature(String signature, Map<String, Object> payload);
}
