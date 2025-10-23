package com.example.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @Value("${app.firebase.service-account-key-path}")
    private Resource serviceAccountKeyResource;

    @Value("${app.firebase.database-url}")
    private String databaseUrl;

    @Bean
    public FirebaseApp initializeFirebaseApp() throws IOException {
        InputStream serviceAccountStream = serviceAccountKeyResource.getInputStream();

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
                .setDatabaseUrl(databaseUrl)
                .build();

        // Tránh khởi tạo lại nếu đã tồn tại (ví dụ khi reload)
        for (FirebaseApp app : FirebaseApp.getApps()) {
            if (app.getName().equals(FirebaseApp.DEFAULT_APP_NAME)) {
                return app;
            }
        }

        return FirebaseApp.initializeApp(options);
    }
}