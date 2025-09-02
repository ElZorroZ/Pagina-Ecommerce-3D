package com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Review;

import lombok.*;

import java.time.LocalDate;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ReviewResponseDTO {
    private Integer id;
    private Integer productId;
    private Integer usuarioId;
    private String mensaje;
    private LocalDate fecha;
    private Integer calificacion; // 1 a 5 estrellas
    private ReviewResponseDTO respuesta; // respuesta única, si la hay
    private String nombre;
    private String apellido;
}
