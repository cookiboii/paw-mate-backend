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
public class CommonErrorDto {
    private int statusCode;
    private String code;
    private String statusMessage;

    public CommonErrorDto(HttpStatus httpStatus, String statusMessage) {
        this.statusCode = httpStatus.value();
        this.code = httpStatus.name();
        this.statusMessage = statusMessage;
    }

    @Builder
    public CommonErrorDto(int statusCode, String code, String statusMessage) {
        this.statusCode = statusCode;
        this.code = code;
        this.statusMessage = statusMessage;
    }

    public static CommonErrorDto of(ErrorCode errorCode) {
        return CommonErrorDto.builder()
                .statusCode(errorCode.getHttpStatus().value())
                .code(errorCode.getCode())
                .statusMessage(errorCode.getMessage())
                .build();
    }

    public static CommonErrorDto of(ErrorCode errorCode, String customMessage) {
        return CommonErrorDto.builder()
                .statusCode(errorCode.getHttpStatus().value())
                .code(errorCode.getCode())
                .statusMessage(customMessage)
                .build();
    }
}

