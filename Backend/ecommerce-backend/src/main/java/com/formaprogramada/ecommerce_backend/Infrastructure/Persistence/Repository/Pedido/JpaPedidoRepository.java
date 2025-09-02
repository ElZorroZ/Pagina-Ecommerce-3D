package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Pedido;


import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Pedido.PedidoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Usuario.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;


public interface JpaPedidoRepository extends JpaRepository<PedidoEntity, Integer> {
    @Transactional
    @Modifying
    @Query("UPDATE PedidoEntity  p SET p.estado= :estado WHERE p.id= :id")
    void modificarEstado(@Param("id") int id, @Param("estado") String estado);

    @Query("SELECT p FROM PedidoEntity p WHERE p.usuarioId= :id")
    List<PedidoEntity> PedidosDeUsuario(@Param("id")UsuarioEntity id);

}
