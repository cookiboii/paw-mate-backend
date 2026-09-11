package com.kindtail.adoptmate.common.dto;

import com.kindtail.adoptmate.common.exception.ErrorCode;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@ToString
@NoArgsConstructor
public class ApiErrorResponse {
    private int statusCode;
    private String code;
    private String statusMessage;

    public ApiErrorResponse(HttpStatus httpStatus, String statusMessage) {
        this.statusCode = httpStatus.value();
        this.code = httpStatus.name();
        this.statusMessage = statusMessage;
    }

    @Builder
    public ApiErrorResponse(int statusCode, String code, String statusMessage) {
        this.statusCode = statusCode;
        this.code = code;
        this.statusMessage = statusMessage;
    }

    public static ApiErrorResponse of(ErrorCode errorCode) {
        return ApiErrorResponse.builder()
                .statusCode(errorCode.getHttpStatus().value())
                .code(errorCode.getCode())
                .statusMessage(errorCode.getMessage())
                .build();
    }

    public static ApiErrorResponse of(ErrorCode errorCode, String customMessage) {
        return ApiErrorResponse.builder()
                .statusCode(errorCode.getHttpStatus().value())
                .code(errorCode.getCode())
                .statusMessage(customMessage)
                .build();
    }
}

