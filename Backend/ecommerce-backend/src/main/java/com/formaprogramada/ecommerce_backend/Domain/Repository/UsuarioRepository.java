package com.formaprogramada.ecommerce_backend.Domain.Repository;
import com.formaprogramada.ecommerce_backend.Domain.Model.Usuario;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository {

    void guardar(Usuario usuario);

    List<Usuario> findAll();

    boolean existePorGmail(String gmail);

    Optional<Usuario> buscarPorGmail(String gmail); // Útil para login, validación o recuperación

    Optional<Usuario> buscarPorId(Integer id); // Útil para ver perfil o gestión

}
