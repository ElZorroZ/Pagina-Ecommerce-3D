package com.formaprogramada.ecommerce_backend.Infrastructure.Elastic;

import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto.JpaProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Profile("prod") // solo se carga en prod
public class ProductoSyncServiceImpl implements ProductoSyncService {

    @Autowired
    private JpaProductoRepository productoRepository;

    @Autowired
    private ProductoSearchRepository productoSearchRepository;

    public void sincronizarProducto(Integer productoId) {
        ProductoEntity producto = productoRepository.findById(productoId).orElse(null);

        if (producto != null) {
            ProductoDocument document = convertirADocument(producto);
            productoSearchRepository.save(document);
        } else {
            productoSearchRepository.deleteById(productoId);
        }
    }

    @Transactional(readOnly = true)
    public void sincronizarTodosLosProductos() {
        System.out.println("Iniciando sincronización completa con Elasticsearch...");

        productoSearchRepository.deleteAll();

        List<ProductoEntity> productos = productoRepository.findAll();

        List<ProductoDocument> documents = productos.stream()
                .map(this::convertirADocument)
                .collect(Collectors.toList());

        productoSearchRepository.saveAll(documents);

        System.out.println("Sincronización completa finalizada. " + documents.size() + " productos indexados.");
    }

    public void eliminarDeIndice(Integer productoId) {
        productoSearchRepository.deleteById(productoId);
    }

    private ProductoDocument convertirADocument(ProductoEntity producto) {
        ProductoDocument document = new ProductoDocument();

        document.setId(producto.getId());
        document.setNombre(producto.getNombre());
        document.setPrecio(producto.getPrecio());

        return document;
    }

    private List<String> generarTags(ProductoEntity producto) {
        List<String> tags = new ArrayList<>();
        if (producto.getNombre() != null) {
            tags.addAll(Arrays.asList(producto.getNombre().toLowerCase().split("\\s+")));
        }
        return tags.stream().distinct().collect(Collectors.toList());
    }
}
