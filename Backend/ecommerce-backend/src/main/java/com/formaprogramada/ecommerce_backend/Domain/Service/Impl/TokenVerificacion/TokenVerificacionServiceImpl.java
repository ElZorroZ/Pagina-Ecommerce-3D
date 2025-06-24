package com.formaprogramada.ecommerce_backend.Domain.Service.Impl.TokenVerificacion;

import com.formaprogramada.ecommerce_backend.Domain.Service.TokenVerificacion.TokenVerificacionService;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.TokenVerificacion.TokenVerificacion;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Usuario.UsuarioEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.TokenVerificacion.TokenVerificacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenVerificacionServiceImpl implements TokenVerificacionService {

    private final TokenVerificacionRepository tokenRepo;

    private static final int DURACION_HORAS = 24; // Duración del token 24 horas

    @Override
    public TokenVerificacion crearTokenParaUsuario(UsuarioEntity usuario) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiracion = LocalDateTime.now().plusHours(DURACION_HORAS);

        TokenVerificacion tokenVerificacion = TokenVerificacion.builder()
                .token(token)
                .usuario(usuario)
                .fechaExpiracion(expiracion)
                .build();

        return tokenRepo.save(tokenVerificacion);
    }

    @Override
    public Optional<TokenVerificacion> validarToken(String token) {
        Optional<TokenVerificacion> tokenOpt = tokenRepo.findByToken(token);

        if (tokenOpt.isEmpty()) {
            return Optional.empty();
        }

        TokenVerificacion tokenVerificacion = tokenOpt.get();

        if (tokenVerificacion.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            tokenRepo.delete(tokenVerificacion);
            return Optional.empty();
        }

        return tokenOpt;
    }
}