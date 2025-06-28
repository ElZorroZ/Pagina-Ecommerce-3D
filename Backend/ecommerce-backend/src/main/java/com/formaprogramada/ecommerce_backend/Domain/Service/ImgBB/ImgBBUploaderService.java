package com.formaprogramada.ecommerce_backend.Domain.Service.ImgBB;

import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.ImgBB.ImgBBData;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ImgBBUploaderService {
    ImgBBData subirImagen(MultipartFile file) throws IOException;
    void borrarImagenDeImgBB(String deleteUrl);

}
