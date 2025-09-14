package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Pedido;

import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoColorEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name ="pedido_producto")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class PedidoProductoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="pedidoId")
    private PedidoEntity pedidoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="productoId")
    private ProductoEntity productoId;

    @Column(length = 100, nullable = false)
    private int cantidad;

    @Column(length = 100, nullable = false)
    private double precio;

    @Column(length = 100, nullable = false)
    private int colorId;

    @Column(length = 50, nullable = false)
    private Boolean esDigital;

    @Column(length = 100, nullable = false)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "colorId", insertable = false, updatable = false)
    private ProductoColorEntity color;

    @Column(length = 100, nullable = false)
    private String nombreColor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productoId", insertable = false, updatable = false)
    private ProductoEntity producto;
}
