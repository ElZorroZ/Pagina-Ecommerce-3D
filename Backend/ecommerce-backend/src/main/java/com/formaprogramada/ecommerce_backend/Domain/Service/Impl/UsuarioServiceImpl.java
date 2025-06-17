package com.formaprogramada.ecommerce_backend.Domain.Service.Impl;
import com.formaprogramada.ecommerce_backend.Domain.Model.Usuario;
import com.formaprogramada.ecommerce_backend.Domain.Repository.UsuarioRepository;
import com.formaprogramada.ecommerce_backend.Domain.Service.UsuarioService;
import com.formaprogramada.ecommerce_backend.Security.Hasher.PasswordHasher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordHasher passwordHasher;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, PasswordHasher passwordHasher) {
        this.usuarioRepository = usuarioRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    @Transactional // asegura consistencia si más adelante agregás acciones adicionales
    public void registrarUsuario(Usuario usuario) {
        validarDatos(usuario);

        if (usuarioRepository.existePorGmail(usuario.getGmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese Gmail.");
        }

        String hash = passwordHasher.hash(usuario.getPassword());
        usuario.setPassword(hash);
        usuario.setVerificado(false);
        usuario.setPermiso(false); // Por defecto es usuario

        usuarioRepository.guardar(usuario);
    }

    private void validarDatos(Usuario usuario) {
        if (usuario.getNombre() == null || usuario.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }

        if (usuario.getApellido() == null || usuario.getApellido().isBlank()) {
            throw new IllegalArgumentException("El apellido es obligatorio.");
        }

        if (usuario.getGmail() == null || usuario.getGmail().isBlank()) {
            throw new IllegalArgumentException("El Gmail es obligatorio.");
        }

        String pass = usuario.getPassword();
        if (pass == null || pass.length() < 8 || pass.length() > 16) {
            throw new IllegalArgumentException("La contraseña debe tener entre 8 y 16 caracteres.");
        }
    }
    @Override
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    @Override
    public boolean existePorGmail(String gmail) {
        return usuarioRepository.buscarPorGmail(gmail).isPresent();
    }

}