package com.formaprogramada.ecommerce_backend.Domain.Service.MercadoPago;

import java.math.BigDecimal;
import java.util.Map;

public interface MercadoPagoService {
    String confirmarPedido(String mercadolibreToken, String title, BigDecimal price, String pedidoId, int quantity, int usuarioId);
    boolean verifySignature(String signature, Map<String, Object> payload);
}
