package com.formaprogramada.ecommerce_backend.Web;

import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoAprobacionRequest;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoResponse;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import com.formaprogramada.ecommerce_backend.Domain.Service.ImgBB.ImgBBUploaderService;
import com.formaprogramada.ecommerce_backend.Domain.Service.Producto.*;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.ImgBB.ImgBBData;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.*;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria.CategoriaEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.*;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto.JpaProductoDetalleRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto.JpaProductoRepository;
import com.formaprogramada.ecommerce_backend.Mapper.Producto.ProductoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@NoArgsConstructor
@AllArgsConstructor
@RequestMapping("/api/productosAprobacion")
public class ProductoAprobacionController {
    @Autowired
    private ProductoAprobadoService productoAprobadoService;

    @Autowired
    private ProductoArchivoService archivoService;

    @Autowired
    private ImgBBUploaderService imgBBUploaderService;

    @Autowired
    private ProductoDestacadoService productoDestacadoService;


    @PostMapping(path = "/crearAprobacionProducto",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductoAprobacionResponse> crearAprobacionProducto(
            @RequestPart("producto") ProductoAprobacionRequest dto,
            @RequestPart(value = "producto", required = false) MultipartFile archivo) throws IOException {

        ProductoAprobacionResponse response = productoAprobadoService.crearAprobacionProducto(dto, archivo);
        System.out.println(response);
        return ResponseEntity.ok(response);
    }
}
