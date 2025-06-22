package com.formaprogramada.ecommerce_backend.Domain.Repository;

import com.formaprogramada.ecommerce_backend.Domain.Model.Categoria;



public interface CategoriaRepository{
    boolean crearCategoria(Categoria categoria);

    boolean existePorNombre(String nombre);

    boolean guardar(Categoria categoria);

}
