package com.chwihae.client.kakao.decoder;

import com.chwihae.exception.CustomException;
import com.chwihae.exception.CustomExceptionError;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import static org.springframework.http.HttpStatus.*;

public class KakaoFeignErrorDecoder implements ErrorDecoder {

    private static final Set<Integer> GOOGLE_AUTHORIZATION_ERROR_STATUS = Set.of(
            BAD_REQUEST.value(),
            UNAUTHORIZED.value(),
            FORBIDDEN.value()
    );

    @Override
    public Exception decode(String methodKey, Response response) {
        if (isGoogleAuthorizationError(response.status())) {
            throw new CustomException(CustomExceptionError.INVALID_KAKAO_AUTHORIZATION_CODE);
        }
        throw new IllegalStateException(getErrorContent(response));
    }

    private boolean isGoogleAuthorizationError(int status) {
        return GOOGLE_AUTHORIZATION_ERROR_STATUS.contains(status);
    }

    private String getErrorContent(Response response) {
        if (response.body() == null) {
            return "Response error body is null";
        }

        try {
            byte[] bodyData = Util.toByteArray(response.body().asInputStream());
            return new String(bodyData, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "Failed to decode error content";
        }
    }
}
