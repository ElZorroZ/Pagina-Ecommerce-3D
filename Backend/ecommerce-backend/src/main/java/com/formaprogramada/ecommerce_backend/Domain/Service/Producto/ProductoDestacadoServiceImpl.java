package com.formaprogramada.ecommerce_backend.Domain.Service.Producto;

import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoDestacadoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto.JpaProductoDestacadoRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto.JpaProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductoDestacadoServiceImpl implements ProductoDestacadoService {
    @Autowired
    private final JpaProductoRepository productoRepository;
    @Autowired
    private final JpaProductoDestacadoRepository destacadoRepository;
    @Override
    @Caching(evict = {
            @CacheEvict(value = "productosDestacados", allEntries = true),
            @CacheEvict(value = "productos", allEntries = true),
            @CacheEvict(value = "producto", key = "#productoId"),
            @CacheEvict(value = "productoCompleto", key = "#productoId")
    })
    @Transactional
    public void toggleProductoDestacado(Integer productoId) {
        ProductoEntity producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        Optional<ProductoDestacadoEntity> existente = destacadoRepository.findByProducto(producto);

        if (existente.isPresent()) {
            destacadoRepository.delete(existente.get());
        } else {
            long cantidadDestacados = destacadoRepository.count();
            if (cantidadDestacados >= 10) {
                throw new MaxDestacadosException("No se puede agregar más de 10 productos destacados");
            }
            ProductoDestacadoEntity nuevo = ProductoDestacadoEntity.builder()
                    .producto(producto)
                    .build();
            destacadoRepository.save(nuevo);
        }
    }
}
