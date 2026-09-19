package com.kindtail.adoptmate.common.dto;

import com.kindtail.adoptmate.common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@ToString
@NoArgsConstructor
@Schema(description = "API 오류 응답 표준 포맷")
public class ApiErrorResponse {
    @Schema(description = "HTTP 상태 코드", example = "403")
    private int statusCode;

    @Schema(description = "업무 오류 코드", example = "P002")
    private String code;

    @Schema(description = "사용자에게 표시할 오류 메시지", example = "게시글 작성자만 수정하거나 삭제할 수 있습니다.")
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

