package com.formaprogramada.ecommerce_backend.Infrastructure.Elastic;

import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Producto.ProductoAprobar.ColorRequest;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Profile("dev")
public interface ProductoSearchRepository extends ElasticsearchRepository<ProductoDocument, Integer> {

    // Búsqueda por nombre
    Page<ProductoDocument> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

    // Búsqueda por rango de precio
    Page<ProductoDocument> findByPrecioBetween(Float precioMin, Float precioMax, Pageable pageable);
}
