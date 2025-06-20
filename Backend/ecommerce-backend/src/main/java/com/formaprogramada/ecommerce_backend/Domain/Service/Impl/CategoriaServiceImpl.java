package com.formaprogramada.ecommerce_backend.Domain.Service.Impl;

import com.formaprogramada.ecommerce_backend.Domain.Model.Categoria;
import com.formaprogramada.ecommerce_backend.Domain.Repository.CategoriaRepository;
import com.formaprogramada.ecommerce_backend.Domain.Service.CategoriaService;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaServiceImpl(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    @Transactional
    public boolean registrarCategoria(Categoria categoria){
        validarDatos(categoria);
        if (categoriaRepository.existePorNombre(categoria.getNombre())){
            throw new IllegalArgumentException("Ya existe una categoria con ese nombre.");
        }

        return categoriaRepository.guardar(categoria);
    }

    private void validarDatos(Categoria categoria) {
        if (categoria.getNombre() == null || categoria.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }

        if (categoria.getDescripcion() == null || categoria.getDescripcion().isBlank()) {
            throw new IllegalArgumentException("La Descripcion es obligatoria.");
        }
    }

}
