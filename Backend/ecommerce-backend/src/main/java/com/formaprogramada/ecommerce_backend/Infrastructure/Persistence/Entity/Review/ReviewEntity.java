package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Review;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "review")
public class ReviewEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "productId", nullable = false)
    private Integer productId;

    @Column(name = "usuarioId", nullable = false)
    private Integer usuarioId;

    @Column(nullable = false, length = 400)
    private String mensaje;

    @Column(nullable = false)
    private LocalDate fecha = LocalDate.now();

    // Relación a la review padre (si es una respuesta)
    @ManyToOne
    @JoinColumn(name = "respuesta_a_id")
    private ReviewEntity respuestaA;

    // Relación inversa: respuestas que pertenecen a esta review
    @OneToMany(mappedBy = "respuestaA", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewEntity> respuestas = new ArrayList<>();

    @Column(nullable = false)
    private Integer calificacion; // valor de 1 a 5 estrellas
}
