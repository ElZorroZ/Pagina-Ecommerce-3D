package com.formaprogramada.ecommerce_backend.Web;

import com.formaprogramada.ecommerce_backend.Domain.Model.Carrito.Carrito;
import com.formaprogramada.ecommerce_backend.Domain.Model.Pedido.Pedido;
import com.formaprogramada.ecommerce_backend.Domain.Service.Carrito.CarritoService;
import com.formaprogramada.ecommerce_backend.Domain.Service.Pedido.PedidoService;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Carrito.CarritoAgregarRequest;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Carrito.CarritoEntity;
import com.formaprogramada.ecommerce_backend.Mapper.Carrito.CarritoMapper;
import com.formaprogramada.ecommerce_backend.Mapper.Pedido.PedidoMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/pedido")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping("/crearPedido")
    public ResponseEntity<Pedido> agregarProductoaCarrito(@Valid @RequestBody List<CarritoEntity> lista) {
        try{

            var pedido= PedidoMapper.toDomain(lista);
            int id=lista.get(0).getUsuarioId();
            Pedido pedidoFinal= pedidoService.CrearPedido(pedido,id);
            return ResponseEntity.ok(pedidoFinal);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
