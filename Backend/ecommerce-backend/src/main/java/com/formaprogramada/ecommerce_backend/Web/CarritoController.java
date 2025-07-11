package com.formaprogramada.ecommerce_backend.Web;
import com.formaprogramada.ecommerce_backend.Domain.Model.Descuento.Carrito;
import com.formaprogramada.ecommerce_backend.Domain.Service.Descuento.CarritoService;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Descuento.CarritoAgregarRequest;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Carrito.CarritoEntity;
import com.formaprogramada.ecommerce_backend.Mapper.Carrito.CarritoMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/carrito")
@RequiredArgsConstructor
public class CarritoController {

    private final CarritoService carritoService;


    @PostMapping("/agregarProductoaCarrito")
    public ResponseEntity<List<Carrito>> agregarProductoaCarrito(@Valid @RequestBody CarritoAgregarRequest carritoAgregarRequest) {
        try{

            var carrito= CarritoMapper.toDomain(carritoAgregarRequest);
            carrito= carritoService.AgregarCarrito(carrito);
            List<Carrito> lista=new ArrayList<Carrito>();
            lista.add(carrito);
            System.out.println(lista);
            return ResponseEntity.ok(lista);
        } catch (Exception e) {
            throw new RuntimeException(e);
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
    public ResponseEntity<?> BorrarProductoaCarrito(
            @PathVariable int id
    ) {
        try {
            return ResponseEntity.ok(carritoService.BorrarProductoCarrito(id));

        } catch (Exception e) {
            throw new RuntimeException(e);
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
    public ResponseEntity<List<CarritoEntity>> VerCarrito(
            @PathVariable int id
    ) {
        try {
            List<CarritoEntity> lista=carritoService.LeerUnCarrito(id);
            System.out.println(lista);
            return ResponseEntity.ok(lista);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }



}
