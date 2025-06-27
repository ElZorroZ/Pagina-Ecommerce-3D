package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository;

import com.formaprogramada.ecommerce_backend.Domain.Model.Categoria.Categoria;
import com.formaprogramada.ecommerce_backend.Domain.Repository.CategoriaRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Categoria.CategoriaEntity;
import com.formaprogramada.ecommerce_backend.Mapper.CategoriaEntityMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public class CategoriaRepositoryImpl implements CategoriaRepository {
    private final JpaCategoriaRepository jpaRepository;
    private final CategoriaEntityMapper mapper;
    private final JpaCategoriaBuscarRepository jpaRepository2;


    public CategoriaRepositoryImpl(JpaCategoriaRepository jpaRepository, CategoriaEntityMapper mapper, JpaCategoriaBuscarRepository jpaRepository2) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
        this.jpaRepository2 = jpaRepository2;
    }

    @Override
    public Categoria guardar(Categoria categoria) {
        CategoriaEntity entity = mapper.toEntity(categoria);
        CategoriaEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public List<CategoriaEntity> LeerTodo(List<CategoriaEntity> lista) {
        Categoria categoria= new Categoria();
        lista=jpaRepository2.findAll();
        return lista;
    }

    @Override
    public Optional<CategoriaEntity> LeerUno(Categoria categoria) {
        Optional<CategoriaEntity> categoria1 = jpaRepository.findById(categoria.getId());
        return categoria1;
    }
}
