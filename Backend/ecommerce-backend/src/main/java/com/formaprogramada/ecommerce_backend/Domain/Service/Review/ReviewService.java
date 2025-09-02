package com.formaprogramada.ecommerce_backend.Domain.Service.Review;

import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Review.ReviewRequestDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Review.ReviewResponseDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Review.ReviewEntity;

import java.util.List;

public interface ReviewService {
    ReviewResponseDTO crearReview(ReviewRequestDTO request);
    ReviewResponseDTO responderReview(Integer reviewId, ReviewRequestDTO request); // <- coincide con la implementación
    void eliminarReview(Integer reviewId);
    public List<ReviewResponseDTO> listarReviewsConRespuestas(Integer productId);
    }
