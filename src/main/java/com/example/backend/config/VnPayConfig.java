package com.example.backend.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "vnpay")
public class VnPayConfig {

    private String url;
    private String returnUrl;
    private String tmnCode;
    private String secretKey;
    private String version;
    private String command;
    private String orderType;
    private String locale;
    private String currencyCode;
}