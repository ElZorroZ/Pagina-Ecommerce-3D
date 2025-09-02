package com.formaprogramada.ecommerce_backend.Domain.Service.Review;

import com.formaprogramada.ecommerce_backend.Domain.Model.Producto.Producto;
import com.formaprogramada.ecommerce_backend.Domain.Model.Usuario.Usuario;
import com.formaprogramada.ecommerce_backend.Domain.Repository.Usuario.UsuarioRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Review.ReviewRequestDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Review.ReviewResponseDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Producto.ProductoEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Review.ReviewEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Usuario.UsuarioEntity;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Producto.JpaProductoRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Review.JpaReviewRepository;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Usuario.JpaUsuarioRepository;
import com.formaprogramada.ecommerce_backend.Mapper.Producto.ProductoMapper;
import com.formaprogramada.ecommerce_backend.Mapper.Review.ReviewMapper;
import com.formaprogramada.ecommerce_backend.Mapper.Usuario.UsuarioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final JpaReviewRepository reviewRepository;
    private final JpaProductoRepository productoRepository;
    private final JpaUsuarioRepository usuarioRepository;
    private final ReviewMapper reviewMapper; // inyectado por Spring
    private final ProductoMapper productoMapper;
    private final UsuarioMapper usuarioMapper;
    private final Map<Integer, List<ReviewResponseDTO>> reviewsCache = new ConcurrentHashMap<>();

    @Override
    public ReviewResponseDTO crearReview(ReviewRequestDTO request) {
        ProductoEntity productoEntity = productoRepository.findById(request.getProductId().intValue())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        UsuarioEntity usuarioEntity = usuarioRepository.findById(request.getUsuarioId().intValue())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        ReviewEntity review = new ReviewEntity();
        review.setProductId(productoEntity.getId());
        review.setUsuarioId(usuarioEntity.getId());
        review.setMensaje(request.getMensaje());
        review.setFecha(LocalDate.now());
        review.setCalificacion(request.getCalificacion());

        ReviewEntity saved = reviewRepository.save(review);

        ReviewResponseDTO dto = reviewMapper.toDTO(saved);
        dto.setNombre(usuarioEntity.getNombre());
        dto.setApellido(usuarioEntity.getApellido());

        // 🔹 Actualizar cache: agregar al final de la lista
        reviewsCache.compute(productoEntity.getId(), (k, lista) -> {
            if (lista == null) {
                lista = new ArrayList<>();
            } else if (!(lista instanceof ArrayList)) {
                // si viene de un stream.toList() u otra inmutable
                lista = new ArrayList<>(lista);
            }
            lista.add(dto);
            return lista;
        });


        return dto;
    }

    @Override
    public ReviewResponseDTO responderReview(Integer reviewId, ReviewRequestDTO request) {
        ReviewEntity original = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review no encontrada"));

        UsuarioEntity usuarioEntity = usuarioRepository.findById(request.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        ReviewEntity respuesta = new ReviewEntity();
        respuesta.setProductId(original.getProductId());
        respuesta.setUsuarioId(usuarioEntity.getId());
        respuesta.setMensaje(request.getMensaje());
        respuesta.setFecha(LocalDate.now());
        respuesta.setRespuestaA(original);
        respuesta.setCalificacion(0);

        ReviewEntity saved = reviewRepository.save(respuesta);

        ReviewResponseDTO respDto = reviewMapper.toDTO(saved);
        respDto.setNombre(usuarioEntity.getNombre());
        respDto.setApellido(usuarioEntity.getApellido());

        // 🔹 Actualizar cache: buscar la review original y setear la respuesta
        reviewsCache.computeIfPresent(original.getProductId(), (k, list) -> {
            list.stream()
                    .filter(r -> r.getId().equals(original.getId()))
                    .findFirst()
                    .ifPresent(r -> r.setRespuesta(respDto));
            return list;
        });

        return respDto;
    }

    @Override
    public void eliminarReview(Integer reviewId) {
        ReviewEntity entity = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review no encontrada"));

        reviewRepository.delete(entity);

        // 🔹 Actualizar cache: remover la review eliminada
        reviewsCache.computeIfPresent(entity.getProductId(), (k, list) -> {
            list.removeIf(r -> r.getId().equals(reviewId) ||
                    (r.getRespuesta() != null && r.getRespuesta().getId().equals(reviewId)));
            return list;
        });
    }

    @Override
    public List<ReviewResponseDTO> listarReviewsConRespuestas(Integer productId) {
        return reviewsCache.computeIfAbsent(productId, this::cargarDesdeBD);
    }

    private List<ReviewResponseDTO> cargarDesdeBD(Integer productId) {
        List<ReviewEntity> reviews = reviewRepository.findByProductId(productId);

        return reviews.stream()
                .map(review -> {
                    ReviewResponseDTO dto = reviewMapper.toDTO(review);

                    if (review.getUsuarioId() != null) {
                        usuarioRepository.findById(review.getUsuarioId()).ifPresent(usuario -> {
                            dto.setNombre(usuario.getNombre());
                            dto.setApellido(usuario.getApellido());
                        });
                    }

                    List<ReviewEntity> respuestas = reviewRepository.findByRespuestaAId(review.getId());
                    if (!respuestas.isEmpty()) {
                        ReviewEntity resp = respuestas.get(0);
                        ReviewResponseDTO respDto = reviewMapper.toDTO(resp);
                        usuarioRepository.findById(resp.getUsuarioId()).ifPresent(usuario -> {
                            respDto.setNombre(usuario.getNombre());
                            respDto.setApellido(usuario.getApellido());
                        });
                        dto.setRespuesta(respDto);
                    }

                    return dto;
                })
                .collect(Collectors.toList()); // <- devuelve ArrayList
    }
}
