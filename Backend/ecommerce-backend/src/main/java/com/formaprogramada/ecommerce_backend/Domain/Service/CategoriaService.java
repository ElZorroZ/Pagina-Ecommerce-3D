package com.formaprogramada.ecommerce_backend.Domain.Service;


import com.formaprogramada.ecommerce_backend.Domain.Model.Categoria.Categoria;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Categoria.CategoriaUpdateRequest;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria.CategoriaEntity;

import java.util.List;
import java.util.Optional;

public interface CategoriaService {

    Categoria CrearCategoria(Categoria categoria);
    List<CategoriaEntity> LeerCategorias(List<CategoriaEntity> lista);
    Optional<CategoriaEntity> LeerCategoria(Categoria categoria);
    Categoria ModificarCategoria(Categoria categoria, int id);
    void BorrarCategoria(int id);

}
