package com.formaprogramada.ecommerce_backend.Domain.Service;
import com.formaprogramada.ecommerce_backend.Domain.Model.Usuario;
import java.util.List;

import java.util.Optional;

public interface UsuarioService {

    Usuario registrarUsuario(Usuario usuario);
    List<Usuario> listarTodos();
    boolean existePorGmail(String gmail);
    Usuario actualizarUsuario(Usuario usuario);
}
