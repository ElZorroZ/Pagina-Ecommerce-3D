package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Pedido;


import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Pedido.PedidoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Usuario.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;


public interface JpaPedidoRepository extends JpaRepository<PedidoEntity, Integer> {
    @Transactional
    @Modifying
    @Query("UPDATE PedidoEntity  p SET p.estado= :estado WHERE p.id= :id")
    void modificarEstado(@Param("id") int id, @Param("estado") String estado);

    @Query("SELECT p FROM PedidoEntity p WHERE p.usuarioId= :id")
    List<PedidoEntity> PedidosDeUsuario(@Param("id")UsuarioEntity id);

    @Query("SELECT p FROM PedidoEntity p LEFT JOIN FETCH p.productos WHERE p.usuarioId.id = :usuarioId")
    List<PedidoEntity> findPedidosConProductosPorUsuario(@Param("usuarioId") Integer usuarioId);
    @Query("""
    SELECT prod 
    FROM ProductoEntity prod
    LEFT JOIN FETCH prod.archivos
    WHERE prod.id IN :ids
""")
    List<ProductoEntity> findProductosConArchivos(@Param("ids") List<Integer> ids);

    Optional<PedidoEntity> findByExternalPaymentId(String externalPaymentId);
    @Query("SELECT DISTINCT p.usuarioId.id FROM PedidoEntity p")
    List<Integer> findAllUsuariosConPedidos();

}
