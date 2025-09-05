package com.formaprogramada.ecommerce_backend.Domain.Service.MercadoPago;

import java.math.BigDecimal;

public interface MercadoPagoService {
    String confirmarPedido (String mercadolibreToken, String title, BigDecimal price,String id, int quantity);
}
