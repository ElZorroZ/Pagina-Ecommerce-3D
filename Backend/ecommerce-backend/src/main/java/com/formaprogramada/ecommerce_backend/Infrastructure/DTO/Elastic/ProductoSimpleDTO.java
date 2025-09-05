package com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Elastic;

public record ProductoSimpleDTO(
        Integer id,
        String nombre,
        Float precio,
        String linkArchivo
) {}
