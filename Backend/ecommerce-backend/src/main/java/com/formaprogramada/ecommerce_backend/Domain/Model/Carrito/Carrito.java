package com.formaprogramada.ecommerce_backend.Domain.Model.Carrito;

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
    private Integer colorId;   // para matchear con lo que envías
    private Boolean esDigital; // 0 o 1
}

