package com.formaprogramada.ecommerce_backend.Mapper.Review;

import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.Review.ReviewResponseDTO;
import com.formaprogramada.ecommerce_backend.Infrastructure.Persistence.Entity.Review.ReviewEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    ReviewMapper INSTANCE = Mappers.getMapper(ReviewMapper.class);

    ReviewResponseDTO toDTO(ReviewEntity review);

    ReviewEntity toEntity(ReviewResponseDTO dto);
}
