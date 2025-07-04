package com.formaprogramada.ecommerce_backend.Domain.Repository;

import com.formaprogramada.ecommerce_backend.Domain.Model.Categoria.Categoria;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria.CategoriaEntity;

import java.util.List;


public interface CategoriaRepository {
    Categoria guardar(Categoria categoria);
    List<CategoriaEntity> LeerTodo(List<CategoriaEntity> lista);
    CategoriaEntity LeerUno(Categoria categoria);
    Categoria modificar(Categoria categoria, int id);
    void borrar(int id);
    String borrarImagen(int id);

}
