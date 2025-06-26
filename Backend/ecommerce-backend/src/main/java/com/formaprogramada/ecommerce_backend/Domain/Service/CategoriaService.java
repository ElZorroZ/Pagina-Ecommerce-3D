package com.formaprogramada.ecommerce_backend.Domain.Service;


import com.formaprogramada.ecommerce_backend.Domain.Model.Categoria;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.CategoriaCrearRequest;

public interface CategoriaService {

    Categoria CrearCategoria(Categoria categoria);

}
