package com.example.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.backend.exception.FileStorageException; // (Chúng ta sẽ tạo file này ở bước 4)

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.apache.commons.io.FilenameUtils; // (Cần 1 dependency, xem bước 3)

@Service
public class FileStorageService {

    private final Path fileStorageLocation;
    private final String serverBaseUrl; // (Ví dụ: http://localhost:8080)
    private final String uploadPathPrefix = "/uploads/images/"; // Phải khớp với WebConfig

    public FileStorageService(
            @Value("${file.upload-dir}") String uploadDir,
            @Value("${app.server-base-url}") String serverBaseUrl) {
                
        this.serverBaseUrl = serverBaseUrl;
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileStorageException("Failed to store empty file.");
        }

        // Tạo tên file duy nhất
        String originalFileName = file.getOriginalFilename();
        String extension = FilenameUtils.getExtension(originalFileName);
        String newFileName = UUID.randomUUID().toString() + "." + extension;

        try {
            Path targetLocation = this.fileStorageLocation.resolve(newFileName);
            
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }
            
            // Trả về URL để client có thể truy cập
            // Ví dụ: http://localhost:8080/uploads/images/uuid.jpg
            return this.serverBaseUrl + this.uploadPathPrefix + newFileName;

        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + newFileName + ". Please try again!", ex);
        }
    }
}