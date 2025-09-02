package com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Repository.Review;

import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Review.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaReviewRepository extends JpaRepository<ReviewEntity, Integer> {
    List<ReviewEntity> findByProductIdAndRespuestaAIsNull(Integer productId);
    List<ReviewEntity> findByRespuestaAId(Integer reviewId);
    List<ReviewEntity> findByProductId(Integer productId);

}
