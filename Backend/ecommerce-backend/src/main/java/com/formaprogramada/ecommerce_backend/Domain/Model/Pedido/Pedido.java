package com.formaprogramada.ecommerce_backend.Domain.Model.Pedido;

import jakarta.persistence.Entity;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Pedido {
    private Integer id;
    private Date fechaPedido;
    private double total;
    private int usuarioId;
    private String estado;
    private String externalPaymentId;
    private String paymentProvider;
}
