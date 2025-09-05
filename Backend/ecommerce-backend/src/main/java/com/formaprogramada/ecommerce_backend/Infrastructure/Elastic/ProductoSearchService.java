package com.formaprogramada.ecommerce_backend.Infrastructure.Elastic;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Elastic.ProductoBusquedaDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.*;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;
import co.elastic.clients.elasticsearch._types.SortOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Profile("prod")
public class ProductoSearchService {
    @Autowired
    private ProductoSearchRepository productoSearchRepository;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    private Sort obtenerSort(String ordenarPor) {
        if (ordenarPor == null || ordenarPor.trim().isEmpty() || ordenarPor.equalsIgnoreCase("relevancia")) {
            return Sort.unsorted(); // Por relevancia
        }

        switch (ordenarPor.toLowerCase()) {
            case "precio_asc":
                return Sort.by(Sort.Direction.ASC, "precio");
            case "precio_desc":
                return Sort.by(Sort.Direction.DESC, "precio");
            case "nombre_asc":
                return Sort.by(Sort.Direction.ASC, "nombre.keyword");
            case "nombre_desc":
                return Sort.by(Sort.Direction.DESC, "nombre.keyword");
            default:
                return Sort.unsorted();
        }
    }

    public Page<ProductoDocument> busquedaAvanzada(ProductoBusquedaDTO filtros, String ordenarPor, Pageable pageable) {
        Criteria criteria = new Criteria();

        // Búsqueda por nombre (query general) usando wildcard
        if (filtros.getQuery() != null && !filtros.getQuery().trim().isEmpty()) {
            String valorWildcard = "*" + filtros.getQuery().trim() + "*"; // permite que coincida parcialmente
            criteria = criteria.or(new Criteria("nombre").expression(valorWildcard));
        }

        // Filtro por nombre exacto
        if (filtros.getNombre() != null && !filtros.getNombre().trim().isEmpty()) {
            criteria = criteria.and(new Criteria("nombre").is(filtros.getNombre()));
        }

        // Filtro por rango de precios
        if (filtros.getPrecioMin() != null) {
            criteria = criteria.and(new Criteria("precio").greaterThanEqual(filtros.getPrecioMin()));
        }
        if (filtros.getPrecioMax() != null) {
            criteria = criteria.and(new Criteria("precio").lessThanEqual(filtros.getPrecioMax()));
        }

        // Aplicar ordenamiento al Pageable
        Sort sort = obtenerSort(ordenarPor);
        Pageable pageableConSort = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sort
        );

        CriteriaQuery query = new CriteriaQuery(criteria, pageableConSort);

        SearchHits<ProductoDocument> searchHits = elasticsearchOperations.search(query, ProductoDocument.class);

        List<ProductoDocument> productos = searchHits.stream()
                .map(hit -> hit.getContent())
                .collect(Collectors.toList());

        return new PageImpl<>(productos, pageableConSort, searchHits.getTotalHits());
    }


    public Page<ProductoDocument> busquedaSimple(String query, String ordenarPor, Pageable pageable) {
        if (query == null || query.trim().isEmpty()) {
            // Aplicar ordenamiento incluso para findAll
            Sort sort = obtenerSort(ordenarPor);
            Pageable pageableConSort = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    sort
            );
            return productoSearchRepository.findAll(pageableConSort);
        }

        ProductoBusquedaDTO filtros = new ProductoBusquedaDTO();
        filtros.setQuery(query);

        return busquedaAvanzada(filtros, ordenarPor, pageable);
    }

    public List<ProductoDocument> obtenerSugerenciasConIds(String texto, int limite) {
        if (texto == null || texto.length() < 2) {
            return new ArrayList<>();
        }

        // Buscar por texto contenido en el nombre
        Criteria criteria = new Criteria("nombre").contains(texto);
        CriteriaQuery query = new CriteriaQuery(criteria);
        query.setMaxResults(limite);

        SearchHits<ProductoDocument> searchHits = elasticsearchOperations.search(query, ProductoDocument.class);

        // Devolver la lista de productos completos (para id + nombre)
        return searchHits.stream()
                .map(SearchHit::getContent)
                .distinct()
                .limit(limite)
                .collect(Collectors.toList());
    }

}
