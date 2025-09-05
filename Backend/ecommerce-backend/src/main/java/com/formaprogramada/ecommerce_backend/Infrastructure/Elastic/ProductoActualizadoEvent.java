package com.formaprogramada.ecommerce_backend.Infrastructure.Elastic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@AllArgsConstructor
@Getter
@Setter
public class ProductoActualizadoEvent {
    private final Integer productoId;
}
