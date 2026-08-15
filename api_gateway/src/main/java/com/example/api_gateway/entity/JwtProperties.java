package com.example.api_gateway.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt")
@Data
@NoArgsConstructor
public class JwtProperties {
    // Cả 3 trường này sẽ được lấy từ file application.properties hoặc application.yml
    // thông qua annotation @ConfigurationProperties với prefix là "jwt"
    private String secret;

    private long accessTokenExpiration;

    private long refreshTokenExpiration;
}
