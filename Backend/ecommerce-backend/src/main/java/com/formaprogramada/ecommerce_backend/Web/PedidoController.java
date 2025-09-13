package com.formaprogramada.ecommerce_backend.Web;

import com.formaprogramada.ecommerce_backend.Domain.Model.Pedido.Pedido;
import com.formaprogramada.ecommerce_backend.Domain.Service.Pedido.PedidoService;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Carrito.CarritoCompletoDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Pedido.PedidoDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Pedido.PedidoUsuarioDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Usuario.UsuarioUpdatePedido;
import com.formaprogramada.ecommerce_backend.Mapper.Pedido.PedidoMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedido")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;
    private final CarritoController carritoController;

    @PostMapping("/crearPedido")
    public ResponseEntity<Pedido> crearPedido(@Valid @RequestBody List<CarritoCompletoDTO> lista) {
        try{

            var pedido= PedidoMapper.toDomain(lista);
            int cantidad=0;
            for(CarritoCompletoDTO carrito:lista){
                cantidad+=carrito.getCantidad();
            }
            int id=lista.get(0).getUsuarioId();
            Pedido pedidoFinal= pedidoService.CrearPedido(pedido,id);

            carritoController.VaciarCarrito(pedidoFinal.getUsuarioId());

            return ResponseEntity.ok(pedidoFinal);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @DeleteMapping("/borrarPedido")
    public ResponseEntity<?> borrarPedido(@RequestParam int id){
        try{
            pedidoService.BorrarPedido(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
        throw new RuntimeException(e);
    }
    }

    @PutMapping("/modificarPedido")
    public ResponseEntity<?> modificarPedido(@RequestBody UsuarioUpdatePedido usuarioCambios){
        try{
            pedidoService.ModificarPedido(usuarioCambios);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/verPedidos")
    public ResponseEntity<List<PedidoDTO>> verPedidos(){
        try{
            return ResponseEntity.ok(pedidoService.verPedidos());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/verPedidosDeUsuario")
    public ResponseEntity<List<PedidoDTO>> verPedidosDeUsuario(@RequestParam int id){
        try{
            return ResponseEntity.ok(pedidoService.verPedidosDeUsuario(id));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    @GetMapping("/verPedido")
    public ResponseEntity<PedidoUsuarioDTO> verPedido(@RequestParam int id){
        try{
            return ResponseEntity.ok(pedidoService.verPedido(id));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PutMapping("/CambiarEstado")
    public ResponseEntity<?> cambiarEstado(@RequestParam String estado,@RequestParam int id) {
        try {
            pedidoService.CambiarEstado(estado, id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PutMapping("/EnviarPedidoOnline")
    public ResponseEntity<List<String>> EnviarPedidoOnline(@RequestParam int idPedido) {
        try {
            List<String> archivos = pedidoService.EnviarPedidoOnline(idPedido);
            return ResponseEntity.ok(archivos);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }









}
