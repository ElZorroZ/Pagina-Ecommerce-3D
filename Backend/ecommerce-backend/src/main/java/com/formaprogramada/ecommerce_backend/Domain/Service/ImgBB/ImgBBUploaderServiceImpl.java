package com.formaprogramada.ecommerce_backend.Domain.Service.ImgBB;

import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.ImgBB.ImgBBData;
import com.formaprogramada.ecommerce_backend.Infrastructure.DTO.ImgBB.ImgBBResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

@Service
public class ImgBBUploaderServiceImpl implements ImgBBUploaderService {

    private final String apiKey = "d92f4cc0aef8bebbb8239bfb8360289d";
    private final RestTemplate restTemplate;

    public ImgBBUploaderServiceImpl() {
        this.restTemplate = new RestTemplate();
    }


    @Override
    public ImgBBData subirImagen(MultipartFile file) throws IOException {
        byte[] bytes = file.getBytes();
        String base64File = Base64.getEncoder().encodeToString(bytes);

        String url = "https://api.imgbb.com/1/upload?key=" + apiKey;

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("image", base64File);

        ImgBBResponse response = restTemplate.postForObject(url, body, ImgBBResponse.class);

        if (response != null && response.getData() != null) {
            return response.getData();
        } else {
            throw new RuntimeException("Error subiendo imagen a ImgBB");
        }
    }



    @Override
    public void borrarImagenDeImgBB(String deleteUrl) {
        if (deleteUrl == null || deleteUrl.isEmpty()) return;
        restTemplate.delete(deleteUrl);
    }

}

