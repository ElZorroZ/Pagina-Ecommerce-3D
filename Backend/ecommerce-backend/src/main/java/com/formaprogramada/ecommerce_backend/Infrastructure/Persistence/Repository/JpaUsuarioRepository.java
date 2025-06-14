package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.UsuarioEntity;
import java.util.Optional;


public interface JpaUsuarioRepository extends JpaRepository<UsuarioEntity, Integer> {
    boolean existsByGmail(String gmail);
    Optional<UsuarioEntity> findByGmail(String gmail);
}
