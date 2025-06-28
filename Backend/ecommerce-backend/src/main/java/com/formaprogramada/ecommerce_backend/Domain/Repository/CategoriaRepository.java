package com.formaprogramada.ecommerce_backend.Domain.Repository;

import com.formaprogramada.ecommerce_backend.Domain.Model.Categoria.Categoria;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Categoria.CategoriaUpdateRequest;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria.CategoriaEntity;

import java.util.List;
import java.util.Optional;


public interface CategoriaRepository {
    Categoria guardar(Categoria categoria);
    List<CategoriaEntity> LeerTodo(List<CategoriaEntity> lista);
    Optional<CategoriaEntity> LeerUno(Categoria categoria);
    Categoria modificar(Categoria categoria, int id);
    void borrar(int id);
}
