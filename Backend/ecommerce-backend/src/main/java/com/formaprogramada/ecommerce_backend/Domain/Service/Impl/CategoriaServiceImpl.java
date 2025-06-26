package com.formaprogramada.ecommerce_backend.Domain.Service.Impl;


import com.formaprogramada.ecommerce_backend.Domain.Model.Categoria;
import com.formaprogramada.ecommerce_backend.Domain.Repository.CategoriaRepository;
import com.formaprogramada.ecommerce_backend.Domain.Service.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {


    private final CategoriaRepository categoriaRepository;

    @Override
    public Categoria CrearCategoria(Categoria categoria) {


        return categoriaRepository.guardar(categoria);
    }

}
