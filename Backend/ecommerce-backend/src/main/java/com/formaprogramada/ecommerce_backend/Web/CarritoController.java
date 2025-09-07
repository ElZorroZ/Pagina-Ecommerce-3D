package com.formaprogramada.ecommerce_backend.Web;
import com.formaprogramada.ecommerce_backend.Domain.Model.Carrito.Carrito;
import com.formaprogramada.ecommerce_backend.Domain.Service.Carrito.CarritoService;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Carrito.CarritoAgregarRequest;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Carrito.CarritoCompletoDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Carrito.CarritoEntity;
import com.formaprogramada.ecommerce_backend.Mapper.Carrito.CarritoMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/carrito")
@RequiredArgsConstructor
public class CarritoController {

    private final CarritoService carritoService;

    @PostMapping("/agregarProductoaCarrito")
    public ResponseEntity<?> agregarProductoaCarrito(@Valid @RequestBody CarritoAgregarRequest carritoAgregarRequest) {
        try {
            var carrito = CarritoMapper.toDomain(carritoAgregarRequest);
            carrito = carritoService.AgregarCarrito(carrito);
            return ResponseEntity.ok(List.of(carrito));
        } catch (ResponseStatusException e) {
            // Capturamos los errores lanzados por AgregarCarrito
            return ResponseEntity
                    .status(e.getStatusCode())  // <- corregido
                    .body(Map.of("error", e.getReason()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            // Otros errores, responder con error 500 y mensaje genérico
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno del servidor"));
        }
    }




    @PutMapping("/sumarCantidad/{id}/{cantidad}")
    public ResponseEntity<?> sumarCantidad(
            @PathVariable int id,
            @PathVariable int cantidad
    ) {
        try {
            return ResponseEntity.ok(carritoService.SumarCantidad(cantidad,id));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @DeleteMapping("/borrarProductoaCarrito/{id}")
    public ResponseEntity<?> BorrarProductoaCarrito(@PathVariable int id) {
        try {
            Boolean eliminado = carritoService.BorrarProductoCarrito(id);
            if (Boolean.TRUE.equals(eliminado)) {
                return ResponseEntity.ok().build(); // 200 OK sin cuerpo
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Producto no encontrado o no pudo ser eliminado");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al eliminar producto");
        }
    }


    @DeleteMapping("/vaciarCarrito/{id}")
    public ResponseEntity<?> VaciarCarrito(
            @PathVariable int id
    ) {
        try {
            return ResponseEntity.ok(carritoService.VaciarCarrito(id));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/verCarrito/{id}")
    public ResponseEntity<List<Integer>> VerCarrito(@PathVariable int id) {
        try {
            // Leer solo los IDs del carrito
            List<Integer> listaIds = carritoService.LeerUnCarrito(id);
            System.out.println("IDs del carrito: " + listaIds);
            return ResponseEntity.ok(listaIds);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping("/verCarritoConImagen/{id}")
    public ResponseEntity<List<CarritoCompletoDTO>> VerCarritoImagen(@PathVariable int id) {
        try {
            List<CarritoCompletoDTO> lista = carritoService.LeerUnCarritoCompleto(id);
            lista.forEach(dto -> System.out.println(dto.getId() + " - " + dto.getNombre()));
            return ResponseEntity.ok(lista);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }



}
