package com.formaprogramada.ecommerce_backend.Domain.Model.Descuento;

import jakarta.persistence.Entity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Carrito {
    private Integer id;
    private Integer productoId;
    private Integer usuarioId;
    private Integer cantidad;
    private double precioTotal;
    private double precioUnitario;

}
