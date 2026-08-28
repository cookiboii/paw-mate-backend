package com.kindtail.adoptmate.common.exception;

import com.kindtail.adoptmate.common.dto.CommonErrorDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.persistence.EntityNotFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 비즈니스 커스텀 예외 처리
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<CommonErrorDto> handleCustomException(CustomException e) {
        log.warn("CustomException occurred: {} - {}", e.getErrorCode().getCode(), e.getMessage());
        ErrorCode errorCode = e.getErrorCode();
        CommonErrorDto response = CommonErrorDto.of(errorCode, e.getMessage());
        return new ResponseEntity<>(response, errorCode.getHttpStatus());
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
     * IllegalArgumentException / IllegalStateException 처리
     */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<CommonErrorDto> handleBadRequestExceptions(RuntimeException e) {
        log.warn("BadRequestException occurred: {}", e.getMessage());
        CommonErrorDto response = new CommonErrorDto(HttpStatus.BAD_REQUEST, e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * @Valid 유효성 검사 실패 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonErrorDto> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.warn("MethodArgumentNotValidException occurred: {}", e.getMessage());
        String errorMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        CommonErrorDto response = CommonErrorDto.of(ErrorCode.INVALID_INPUT_VALUE, errorMessage);
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
            org.springframework.orm.ObjectOptimisticLockingFailureException.class,
            jakarta.persistence.OptimisticLockException.class
    })
    public ResponseEntity<CommonErrorDto> handleOptimisticLockException(Exception e) {
        log.warn("OptimisticLockException occurred: {}", e.getMessage());
        CommonErrorDto response = CommonErrorDto.of(ErrorCode.CONCURRENT_UPDATE_CONFLICT);
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    /**
     * DB 데이터 무결성 위반 (Unique Constraint 등) 예외 처리
     */
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<CommonErrorDto> handleDataIntegrityViolationException(org.springframework.dao.DataIntegrityViolationException e) {
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
