package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.UsuarioEntity;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface JpaUsuarioRepository extends JpaRepository<UsuarioEntity, Integer> {
    boolean existsByGmail(String gmail);
    Optional<UsuarioEntity> findByGmail(String gmail);
    @Procedure(name = "actualizarUsuario")
    void actualizarUsuario(
            @Param("p_gmail") String gmail,
            @Param("p_nombre") String nombre,
            @Param("p_apellido") String apellido,
            @Param("p_direccion") String direccion,
            @Param("p_cp") String cp,
            @Param("p_ciudad") String ciudad,
            @Param("p_telefono") String telefono
    );

}
