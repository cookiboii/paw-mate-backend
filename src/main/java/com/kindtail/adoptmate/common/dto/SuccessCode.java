package com.kindtail.adoptmate.common.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SuccessCode {

    // Common
    OK(HttpStatus.OK, "S001", "성공"),
    CREATED(HttpStatus.CREATED, "S002", "생성 성공"),

    // Member
    MEMBER_REGISTER_SUCCESS(HttpStatus.CREATED, "M101", "회원가입 성공"),
    LOGIN_SUCCESS(HttpStatus.OK, "M102", "Login Success"),
    TOKEN_REISSUE_SUCCESS(HttpStatus.OK, "M103", "토큰 재발급 성공"),
    LOGOUT_SUCCESS(HttpStatus.OK, "M104", "로그아웃 성공"),
    MEMBER_INFO_SUCCESS(HttpStatus.OK, "M105", "내 정보 조회 성공"),
    MEMBER_ALL_SUCCESS(HttpStatus.OK, "M106", "전체조회"),
    PASSWORD_CHANGE_SUCCESS(HttpStatus.OK, "M107", "비밀번호 변경 완료"),
    MEMBER_DELETE_SUCCESS(HttpStatus.OK, "M108", "회원 탈퇴 완료"),
    ADMIN_MEMBER_DELETE_SUCCESS(HttpStatus.OK, "M109", "관리자에 의해 회원이 삭제되었습니다."),

    // Email & Password Reset
    EMAIL_SEND_SUCCESS(HttpStatus.OK, "E101", "인증 코드가 이메일로 전송되었습니다."),
    EMAIL_VERIFY_SUCCESS(HttpStatus.OK, "E102", "이메일 인증 완료!"),
    RESET_CODE_SEND_SUCCESS(HttpStatus.OK, "E103", "인증 코드가 이메일로 전송되었습니다."),
    RESET_CODE_VERIFY_SUCCESS(HttpStatus.OK, "E104", "인증 성공"),
    PASSWORD_RESET_SUCCESS(HttpStatus.OK, "E105", "비밀번호가 성공적으로 변경되었습니다."),

    // Animal
    ANIMAL_REGISTER_SUCCESS(HttpStatus.CREATED, "A101", "등록 성공"),
    ANIMAL_LIST_SUCCESS(HttpStatus.OK, "A102", "동물 목록 조회 성공"),
    ANIMAL_SPECIES_LIST_SUCCESS(HttpStatus.OK, "A103", "종별 동물 목록 조회 성공"),
    ANIMAL_DETAIL_SUCCESS(HttpStatus.OK, "A104", "상세 조회 성공"),
    ANIMAL_STATUS_UPDATE_SUCCESS(HttpStatus.OK, "A105", "상태가 성공적으로 변경되었습니다."),
    ANIMAL_DELETE_SUCCESS(HttpStatus.OK, "A106", "삭제 성공"),
    ANIMAL_FAVORITE_TOGGLE_SUCCESS(HttpStatus.OK, "A107", "관심 동물 상태가 성공적으로 변경되었습니다."),
    ANIMAL_FAVORITE_LIST_SUCCESS(HttpStatus.OK, "A108", "관심 동물 목록 조회 성공"),
    ANIMAL_FAVORITE_DELETE_SUCCESS(HttpStatus.OK, "A109", "관심 동물이 찜 목록에서 삭제되었습니다."),

    // Adoption
    ADOPTION_APPLY_SUCCESS(HttpStatus.CREATED, "AD101", "입양 신청이 완료되었습니다."),
    ADOPTION_MY_LIST_SUCCESS(HttpStatus.OK, "AD102", "내 입양 내역 조회 성공"),
    ADOPTION_ALL_SUCCESS(HttpStatus.OK, "AD103", "전체조회"),
    ADOPTION_PAGE_SUCCESS(HttpStatus.OK, "AD104", "전체 입양 목록 조회 성공"),
    ADOPTION_STATUS_UPDATE_SUCCESS(HttpStatus.OK, "AD105", "상태변경완료"),

    // Post
    POST_CREATE_SUCCESS(HttpStatus.CREATED, "P101", "글쓰기완료"),
    POST_LIST_SUCCESS(HttpStatus.OK, "P102", "조회완료"),
    POST_DETAIL_SUCCESS(HttpStatus.OK, "P103", "조회완료"),
    POST_DELETE_SUCCESS(HttpStatus.OK, "P104", "삭제완료"),
    POST_UPDATE_SUCCESS(HttpStatus.OK, "P105", "글수정완료"),

    // Comment
    COMMENT_CREATE_SUCCESS(HttpStatus.CREATED, "CM101", "댓글등록성공"),
    COMMENT_LIST_SUCCESS(HttpStatus.OK, "CM102", "보기성공"),
    COMMENT_DELETE_SUCCESS(HttpStatus.OK, "CM103", "댓글삭제성공"),
    COMMENT_UPDATE_SUCCESS(HttpStatus.OK, "CM104", "수정성공");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
