package com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Review;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequestDTO {
    private Integer productId;
    private Integer usuarioId;
    private String mensaje;
    private Integer calificacion; // 1 a 5 estrellas
}
