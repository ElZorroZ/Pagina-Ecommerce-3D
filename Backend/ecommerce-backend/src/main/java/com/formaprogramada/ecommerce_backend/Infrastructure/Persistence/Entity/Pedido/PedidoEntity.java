package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Pedido;

import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Usuario.UsuarioEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name ="pedido")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class PedidoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;


    @Temporal(TemporalType.DATE)
    @Column(name = "fechaPedido")
    private Date fechaPedido;


    @Column(length = 100, nullable = false)
    private double total;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuarioId")
    private UsuarioEntity usuarioId;

    @Column(length = 50, nullable = false)
    private String estado;

    @Column(name = "external_payment_id")
    private String externalPaymentId;

    @Column(name = "payment_provider")
    private String paymentProvider;

    @OneToMany(mappedBy = "pedidoId", fetch = FetchType.LAZY)
    private List<PedidoProductoEntity> productos = new ArrayList<>();


}