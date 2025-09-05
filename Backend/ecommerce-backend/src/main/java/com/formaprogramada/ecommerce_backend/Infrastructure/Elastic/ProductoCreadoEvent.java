package com.formaprogramada.ecommerce_backend.Infrastructure.Elastic;

import lombok.*;

@Data
@AllArgsConstructor
@Getter
@Setter
public class ProductoCreadoEvent {
    private final Integer productoId;
}
