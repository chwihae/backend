package com.chwihae.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum CustomExceptionError {

    // 400-499: 기본 HTTP 에러
    INVALID_ARGUMENT(HttpStatus.BAD_REQUEST, 400, "파라미터가 올바르지 않습니다"),
    FORBIDDEN(HttpStatus.FORBIDDEN, 403, "권한이 없습니다"),
    NOT_FOUND(HttpStatus.NOT_FOUND, 404, "존재하지 않는 리소스에 대한 요청입니다"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, 405, "올바르지 않은 요청 메소드입니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 500, "서버 내부 오류"),

    // 1000-1099: 인증과 관련된 에러
    INVALID_KAKAO_AUTHORIZATION_CODE(HttpStatus.UNAUTHORIZED, 1000, "유효하지 않은 인가 코드입니다"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, 1001, "유효하지 않은 토큰입니다"),

    // 1100-1199: 사용자와 관련된 에러
    NICKNAME_DUPLICATED(HttpStatus.CONFLICT, 1101, "이미 존재하는 닉네임입니다"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, 1102, "존재하지 않는 사용자입니다");

    private final HttpStatus httpStatus;
    private final int errorCode;
    private final String errorMsg;

    public int code() {
        return this.errorCode;
    }

    public String message() {
        return this.errorMsg;
    }

    public HttpStatus status() {
        return this.httpStatus;
    }
}
