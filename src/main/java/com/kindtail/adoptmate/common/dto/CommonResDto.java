package com.kindtail.adoptmate.common.dto;

import org.springframework.http.HttpStatus;

/**
 * 공통 API 응답 불변 Record DTO
 */
public record CommonResDto(
        int statusCode,
        String statusMessage,
        Object result
) {

    public CommonResDto(HttpStatus httpStatus, String statusMessage, Object result) {
        this(httpStatus.value(), statusMessage, result);
    }

    public static CommonResDto ok(String statusMessage, Object result) {
        return new CommonResDto(HttpStatus.OK, statusMessage, result);
    }

    public static CommonResDto created(String statusMessage, Object result) {
        return new CommonResDto(HttpStatus.CREATED, statusMessage, result);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public Object getResult() {
        return result;
    }
}
