package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.TokenVerificacion;

import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Usuario.UsuarioEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "token_verificacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenVerificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)  // Corregí el nombre de la columna
    private UsuarioEntity usuario;

    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

}
