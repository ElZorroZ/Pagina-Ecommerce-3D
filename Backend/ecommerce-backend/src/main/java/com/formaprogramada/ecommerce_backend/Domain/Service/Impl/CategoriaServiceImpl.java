package com.formaprogramada.ecommerce_backend.Domain.Service.Impl;


import com.formaprogramada.ecommerce_backend.Domain.Model.Categoria.Categoria;
import com.formaprogramada.ecommerce_backend.Domain.Repository.CategoriaRepository;
import com.formaprogramada.ecommerce_backend.Domain.Service.CategoriaService;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Categoria.CategoriaUpdateRequest;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria.CategoriaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {


    private final CategoriaRepository categoriaRepository;

    @Override
    public Categoria CrearCategoria(Categoria categoria) {


        return categoriaRepository.guardar(categoria);
    }

    @Override
    public List<CategoriaEntity> LeerCategorias(List<CategoriaEntity> lista) {

        lista=categoriaRepository.LeerTodo(lista);
        return lista;
    }

    @Override
    public Optional<CategoriaEntity> LeerCategoria(Categoria categoria) {


        return categoriaRepository.LeerUno(categoria);
    }

    @Override
    public Categoria ModificarCategoria(Categoria categoria, int id) {


        return categoriaRepository.modificar(categoria,id);
    }


}
