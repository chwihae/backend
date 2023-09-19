package com.chwihae.config.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "kakao")
@Validated
@Setter
@Getter
@Component
public class KakaoProperties {

    @NotBlank(message = "Kakao Client ID must not be blank")
    private String clientId;
    
    private final Oauth2 oauth2 = new Oauth2();
    private final UserInfo userInfo = new UserInfo();

    @Setter
    @Getter
    public static class Oauth2 {
        @NotBlank(message = "OAuth2 endpoint must not be blank")
        private String endpoint;
    }

    @Setter
    @Getter
    public static class UserInfo {
        @NotBlank(message = "User endpoint must not be blank")
        private String endpoint;
    }
}
