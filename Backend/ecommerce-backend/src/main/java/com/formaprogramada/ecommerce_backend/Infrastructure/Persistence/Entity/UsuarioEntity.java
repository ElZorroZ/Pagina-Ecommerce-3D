package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(nullable = false, length = 200, unique = true)
    private String gmail;

    @Column(nullable = false, length = 64)
    private String password;

    @Column(nullable = false)
    private Boolean permiso;

}
