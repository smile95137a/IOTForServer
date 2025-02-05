package src.main.java.com.frontend.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import org.springframework.core.io.ClassPathResource;

public class ImageUtils {

    public static String imageToBase64(String imagePath) throws IOException {
        ClassPathResource classPathResource = new ClassPathResource(imagePath);
        try (InputStream inputStream = classPathResource.getInputStream()) {
            byte[] bytes = inputStream.readAllBytes();
            return Base64.getEncoder().encodeToString(bytes);
        }
    }
}