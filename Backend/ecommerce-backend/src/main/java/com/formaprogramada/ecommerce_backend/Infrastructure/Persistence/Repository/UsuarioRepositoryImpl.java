package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository;
import com.formaprogramada.ecommerce_backend.Domain.Model.Usuario;
import com.formaprogramada.ecommerce_backend.Domain.Repository.UsuarioRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.UsuarioEntity;
import com.formaprogramada.ecommerce_backend.Mapper.UsuarioEntityMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UsuarioRepositoryImpl implements UsuarioRepository {

    private final JpaUsuarioRepository jpaRepository;
    private final UsuarioEntityMapper mapper;

    public UsuarioRepositoryImpl(JpaUsuarioRepository jpaRepository, UsuarioEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public void guardar(Usuario usuario) {
        UsuarioEntity entity = mapper.toEntity(usuario);
        jpaRepository.save(entity);
    }

    @Override
    public boolean existePorGmail(String gmail) {
        return jpaRepository.existsByGmail(gmail);
    }

    @Override
    public Optional<Usuario> buscarPorGmail(String gmail) {
        return jpaRepository.findByGmail(gmail)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Usuario> buscarPorId(Integer id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Usuario> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}

