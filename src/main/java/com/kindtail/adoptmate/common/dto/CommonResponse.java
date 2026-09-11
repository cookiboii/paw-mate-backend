package com.kindtail.adoptmate.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;

/**
 * 공통 API 응답 불변 Record DTO (Generic)
 */
@Schema(description = "공통 API 응답 표준 포맷")
public record CommonResponse<T>(
        @Schema(description = "HTTP 상태 코드", example = "200")
        int statusCode,

        @Schema(description = "업무 성공 코드", example = "A101")
        String code,

        @Schema(description = "응답 상태 메시지", example = "성공")
        String statusMessage,

        @Schema(description = "응답 데이터 본문")
        T result
) {

    public CommonResponse(HttpStatus httpStatus, String statusMessage, T result) {
        this(httpStatus.value(), httpStatus.name(), statusMessage, result);
    }

    public static <T> CommonResponse<T> ok(String statusMessage, T result) {
        return new CommonResponse<>(HttpStatus.OK, statusMessage, result);
    }

    public static <T> CommonResponse<T> ok(String statusMessage) {
        return new CommonResponse<>(HttpStatus.OK, statusMessage, null);
    }

    public static <T> CommonResponse<T> created(String statusMessage, T result) {
        return new CommonResponse<>(HttpStatus.CREATED, statusMessage, result);
    }

    public static <T> CommonResponse<T> of(SuccessCode successCode, T result) {
        return new CommonResponse<>(successCode.getHttpStatus().value(), successCode.getCode(), successCode.getMessage(), result);
    }

    public static <T> CommonResponse<T> of(SuccessCode successCode) {
        return new CommonResponse<>(successCode.getHttpStatus().value(), successCode.getCode(), successCode.getMessage(), null);
    }

    public static <T> org.springframework.http.ResponseEntity<CommonResponse<T>> toResponseEntity(SuccessCode successCode, T result) {
        return org.springframework.http.ResponseEntity.status(successCode.getHttpStatus()).body(of(successCode, result));
    }

    public static <T> org.springframework.http.ResponseEntity<CommonResponse<T>> toResponseEntity(SuccessCode successCode) {
        return org.springframework.http.ResponseEntity.status(successCode.getHttpStatus()).body(of(successCode));
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public T getResult() {
        return result;
    }
}
