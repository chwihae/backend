package com.chwihae.config.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "jwt")
@Validated
@Setter
@Getter
@Component
public class JwtTokenProperties {

    @NotBlank(message = "JWT Secret Key must not be blank")
    private String secretKey;

    @NotNull(message = "JWT Token Expired Time must not be null")
    private Long tokenExpiredTimeMs;
}
