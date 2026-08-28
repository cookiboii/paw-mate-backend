package com.kindtail.adoptmate.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "유효하지 않은 입력값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "지원하지 않는 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "서버 내부 오류가 발생했습니다."),

    // Member
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "존재하지 않는 회원입니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "M002", "이미 존재하는 이메일입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "M003", "비밀번호가 일치하지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "M004", "인증 정보가 유효하지 않습니다."),
    LOGOUT_TOKEN(HttpStatus.UNAUTHORIZED, "M005", "이미 로그아웃 처리된 토큰입니다."),
    UNAUTHORIZED_AUTHOR(HttpStatus.FORBIDDEN, "C004", "본인 또는 관리자만 수정/삭제 권한이 있습니다."),

    // Animal
    ANIMAL_NOT_FOUND(HttpStatus.NOT_FOUND, "A001", "존재하지 않는 보호 동물입니다."),
    INVALID_ANIMAL_STATUS(HttpStatus.BAD_REQUEST, "A002", "유효하지 않은 동물 상태입니다."),

    // Adoption
    ADOPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "AD001", "존재하지 않는 입양 신청입니다."),
    ADOPTION_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "AD002", "이미 입양 신청한 동물입니다."),
    NOT_PROTECTED_ANIMAL(HttpStatus.BAD_REQUEST, "AD003", "보호 중인 동물만 입양 신청이 가능합니다."),
    INVALID_ADOPTION_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "AD004", "대기 중(PENDING)인 신청만 승인 또는 반려 처리가 가능합니다."),

    // Post & Comment
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "존재하지 않는 게시글입니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "CM001", "존재하지 않는 댓글입니다."),

    // Lock
    LOCK_ACQUISITION_FAILED(HttpStatus.CONFLICT, "L001", "요청이 집중되어 처리에 실패했습니다. 잠시 후 다시 시도해주세요."),
    CONCURRENT_UPDATE_CONFLICT(HttpStatus.CONFLICT, "L002", "다른 요청에 의해 데이터가 이미 변경되었습니다. 최신 정보를 확인 후 다시 시도해주세요.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
