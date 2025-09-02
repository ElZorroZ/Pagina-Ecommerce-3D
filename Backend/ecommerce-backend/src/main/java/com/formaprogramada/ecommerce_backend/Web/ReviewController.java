package com.formaprogramada.ecommerce_backend.Web;

import com.formaprogramada.ecommerce_backend.Domain.Service.Review.ReviewService;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Review.ReviewRequestDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Review.ReviewResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // Crear review (solo CLIENTE)
    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<ReviewResponseDTO> crearReview(@RequestBody @Valid ReviewRequestDTO request) {
        ReviewResponseDTO review = reviewService.crearReview(request);
        return ResponseEntity.ok(review);
    }

    @PostMapping("/{reviewId}/responder")
    @PreAuthorize("hasAnyRole('COLABORADOR','ADMIN')")
    public ResponseEntity<ReviewResponseDTO> responderReview(
            @PathVariable Integer reviewId,
            @RequestBody @Valid ReviewRequestDTO request) {

        System.out.println("Controlador - reviewId recibido: " + reviewId);
        System.out.println("Controlador - request usuarioId: " + request.getUsuarioId());
        System.out.println("Controlador - request mensaje: " + request.getMensaje());

        ReviewResponseDTO response = reviewService.responderReview(reviewId, request);

        System.out.println("Controlador - respuesta enviada al frontend, id: " + response.getId());
        return ResponseEntity.ok(response);
    }


    // Eliminar review (solo ADMIN)
    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarReview(@PathVariable Integer reviewId) {
        reviewService.eliminarReview(reviewId);
        return ResponseEntity.noContent().build();
    }

    // Listar reviews de un producto con sus respuestas
    @GetMapping("/producto/{productId}")
    public ResponseEntity<List<ReviewResponseDTO>> listarReviews(@PathVariable Integer productId) {
        List<ReviewResponseDTO> reviews = reviewService.listarReviewsConRespuestas(productId);
        return ResponseEntity.ok(reviews);
    }

}

