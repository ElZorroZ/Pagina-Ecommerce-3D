package com.formaprogramada.ecommerce_backend.Domain.Repository.Categoria;

import com.formaprogramada.ecommerce_backend.Domain.Model.Categoria.Categoria;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria.CategoriaEntity;

import java.util.Map;
import java.util.Optional;


public interface CategoriaRepository {
    Categoria guardar(Categoria categoria);
    Map<CategoriaEntity,String> LeerTodo( Map<CategoriaEntity,String> lista);
    Map<CategoriaEntity,String> LeerUno(Categoria categoria);
    CategoriaEntity LeerUnoSinImagen(Categoria categoria);
    Categoria modificar(Categoria categoria, int id);
    void borrar(int id);
    String borrarImagen(int categoriaId);
    boolean AgregarDestacado(CategoriaEntity id);
    Optional<Categoria> buscarPorNombreIgnoreCase(String nombre);

}
