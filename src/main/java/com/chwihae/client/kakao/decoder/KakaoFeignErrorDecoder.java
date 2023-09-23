package com.chwihae.client.kakao.decoder;

import com.chwihae.exception.CustomException;
import com.chwihae.exception.CustomExceptionError;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
public class KakaoFeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        if (isClientRequestError(response.status())) {
            log.error("Kakao authorization request error detected for method {}: Response status: {}, Content: {}",
                    methodKey, response.status(), getErrorContent(response));
            throw new CustomException(CustomExceptionError.INVALID_KAKAO_AUTHORIZATION_CODE);
        }
        throw new IllegalStateException(getErrorContent(response));
    }

    private boolean isClientRequestError(int status) {
        return 400 <= status && status < 500;
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
