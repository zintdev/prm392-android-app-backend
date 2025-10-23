package com.example.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;
    
    // Phải khớp với uploadPathPrefix trong FileStorageService
    private final String uploadPathPrefix = "/uploads/images/"; 

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Khi client gọi URL /uploads/images/ten-file.jpg
        // Spring sẽ tìm file vật lý trong thư mục (ví dụ: file:./uploads/images/ten-file.jpg)
        registry.addResourceHandler(uploadPathPrefix + "**")
                .addResourceLocations("file:" + uploadDir + "/");
    }
}