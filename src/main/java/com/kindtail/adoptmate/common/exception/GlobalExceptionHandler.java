package com.kindtail.adoptmate.common.exception;

import com.kindtail.adoptmate.common.dto.CommonErrorDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import jakarta.persistence.OptimisticLockException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 비즈니스 커스텀 예외 처리
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<CommonErrorDto> handleCustomException(CustomException e) {
        log.warn("CustomException occurred: [{}] {}", e.getErrorCode().getCode(), e.getMessage());
        ErrorCode errorCode = e.getErrorCode();
        CommonErrorDto response = CommonErrorDto.of(errorCode, e.getMessage());
        return new ResponseEntity<>(response, errorCode.getHttpStatus());
    }

    /**
     * @Valid 유효성 검사 실패 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonErrorDto> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.warn("MethodArgumentNotValidException occurred: {}", e.getMessage());
        BindingResult bindingResult = e.getBindingResult();
        FieldError fieldError = bindingResult.getFieldError();
        String errorMessage = fieldError != null
                ? String.format("[%s] %s", fieldError.getField(), fieldError.getDefaultMessage())
                : bindingResult.getAllErrors().get(0).getDefaultMessage();

        CommonErrorDto response = CommonErrorDto.of(ErrorCode.INVALID_INPUT_VALUE, errorMessage);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Spring Security 인가 실패 (@PreAuthorize 권한 부족) 처리
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<CommonErrorDto> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("AccessDeniedException occurred: {}", e.getMessage());
        CommonErrorDto response = CommonErrorDto.of(ErrorCode.UNAUTHORIZED_AUTHOR);
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    /**
     * Spring Security 인증 실패 처리
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<CommonErrorDto> handleAuthenticationException(AuthenticationException e) {
        log.warn("AuthenticationException occurred: {}", e.getMessage());
        CommonErrorDto response = CommonErrorDto.of(ErrorCode.UNAUTHORIZED);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    /**
     * EntityNotFoundException 처리
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<CommonErrorDto> handleEntityNotFoundException(EntityNotFoundException e) {
        log.warn("EntityNotFoundException occurred: {}", e.getMessage());
        CommonErrorDto response = new CommonErrorDto(HttpStatus.NOT_FOUND, e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * 요청 파라미터 타입 불일치 (@PathVariable, @RequestParam 타입 변환 실패)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<CommonErrorDto> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.warn("MethodArgumentTypeMismatchException occurred: {}", e.getMessage());
        String message = String.format("파라미터 '%s'의 형식이 올바르지 않습니다.", e.getName());
        CommonErrorDto response = CommonErrorDto.of(ErrorCode.INVALID_INPUT_VALUE, message);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * 필수 요청 파라미터 누락
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<CommonErrorDto> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.warn("MissingServletRequestParameterException occurred: {}", e.getMessage());
        String message = String.format("필수 요청 파라미터 '%s'가 누락되었습니다.", e.getParameterName());
        CommonErrorDto response = CommonErrorDto.of(ErrorCode.INVALID_INPUT_VALUE, message);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * HTTP Request Body 역직렬화 실패 (JSON 형식 오류 등)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<CommonErrorDto> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("HttpMessageNotReadableException occurred: {}", e.getMessage());
        CommonErrorDto response = CommonErrorDto.of(ErrorCode.INVALID_INPUT_VALUE, "요청 본문(JSON)의 형식이 올바르지 않습니다.");
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * 파일 업로드 용량 초과
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<CommonErrorDto> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.warn("MaxUploadSizeExceededException occurred: {}", e.getMessage());
        CommonErrorDto response = CommonErrorDto.of(ErrorCode.INVALID_INPUT_VALUE, "업로드 파일 크기 제한(10MB)을 초과했습니다.");
        return new ResponseEntity<>(response, HttpStatus.PAYLOAD_TOO_LARGE);
    }

    /**
     * IllegalArgumentException / IllegalStateException 처리
     */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<CommonErrorDto> handleBadRequestExceptions(RuntimeException e) {
        log.warn("BadRequestException occurred: {}", e.getMessage());
        CommonErrorDto response = new CommonErrorDto(HttpStatus.BAD_REQUEST, e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * 지원하지 않는 HTTP 메서드 요청 처리
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<CommonErrorDto> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("HttpRequestMethodNotSupportedException occurred: {}", e.getMessage());
        CommonErrorDto response = CommonErrorDto.of(ErrorCode.METHOD_NOT_ALLOWED);
        return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * 낙관적 락 충돌 예외 처리 (OptimisticLockingFailureException)
     */
    @ExceptionHandler({
            ObjectOptimisticLockingFailureException.class,
            OptimisticLockException.class
    })
    public ResponseEntity<CommonErrorDto> handleOptimisticLockException(Exception e) {
        log.warn("OptimisticLockException occurred: {}", e.getMessage());
        CommonErrorDto response = CommonErrorDto.of(ErrorCode.CONCURRENT_UPDATE_CONFLICT);
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    /**
     * DB 데이터 무결성 위반 (Unique Constraint 등) 예외 처리
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<CommonErrorDto> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.warn("DataIntegrityViolationException occurred: {}", e.getMessage());
        CommonErrorDto response = CommonErrorDto.of(ErrorCode.INVALID_INPUT_VALUE, "중복된 데이터가 존재하거나 제약조건을 위반했습니다.");
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    /**
     * 최상위 기타 예외 처리 (500 Internal Server Error)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonErrorDto> handleException(Exception e) {
        log.error("Unhandled Exception occurred: ", e);
        CommonErrorDto response = CommonErrorDto.of(ErrorCode.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
