package com.chwihae.client.kakao.decoder;

import com.chwihae.exception.CustomException;
import feign.Request;
import feign.Response;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static com.chwihae.exception.CustomExceptionError.INVALID_KAKAO_AUTHORIZATION_CODE;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KakaoFeignErrorDecoderTest {

    @Test
    @DisplayName("응답이 400이면 예외를 던진다")
    void decode_whenResponseIs400_returnsCustomException() {
        //given
        KakaoFeignErrorDecoder decoder = new KakaoFeignErrorDecoder();

        Request request = Request.create(Request.HttpMethod.GET, "http://test.com", Collections.emptyMap(), Request.Body.empty(), new feign.RequestTemplate());

        Response response = Response.builder()
                .status(400)
                .reason("Bad Request")
                .headers(Collections.emptyMap())
                .body("Error Message", StandardCharsets.UTF_8)
                .request(request)
                .build();

        //when
        CustomException exception = assertThrows(
                CustomException.class,
                () -> decoder.decode("methodKey", response)
        );

        //then
        Assertions.assertThat(exception)
                .extracting("error")
                .isEqualTo(INVALID_KAKAO_AUTHORIZATION_CODE);
    }

    @Test
    @DisplayName("응답이 500이면 IllegalStateException 예외를 던진다")
    void decode_whenResponseIs500_returnsIllegalStateException() {
        //given
        KakaoFeignErrorDecoder decoder = new KakaoFeignErrorDecoder();

        Request request = Request.create(Request.HttpMethod.GET, "http://test.com", Collections.emptyMap(), Request.Body.empty(), new feign.RequestTemplate());

        String errorMessage = "Internal Server Error";

        Response response = Response.builder()
                .status(500)
                .reason("Server Error")
                .headers(Collections.emptyMap())
                .body(errorMessage, StandardCharsets.UTF_8)
                .request(request)
                .build();

        //when
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> decoder.decode("methodkey", response)
        );

        //then
        Assertions.assertThat(exception.getMessage())
                .isEqualTo(errorMessage);
    }

}