package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Pedido;


import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Pedido.PedidoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;


public interface JpaPedidoRepository extends JpaRepository<PedidoEntity, Integer> {
}
