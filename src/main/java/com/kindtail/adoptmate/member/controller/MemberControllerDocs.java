package com.kindtail.adoptmate.member.controller;

import com.kindtail.adoptmate.auth.CustomUserDetails;
import com.kindtail.adoptmate.common.dto.CommonResDto;
import com.kindtail.adoptmate.member.dto.MemberLoginRequestDto;
import com.kindtail.adoptmate.member.dto.MemberRegisterRequestDto;
import com.kindtail.adoptmate.member.dto.PasswordChangeRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@Tag(name = "1. 회원 및 인증 API", description = "회원가입, 로그인, 토큰 재발급, 로그아웃, 회원 정보 조회 및 탈퇴 API")
public interface MemberControllerDocs {

    @Operation(summary = "일반 회원가입", description = "새로운 회원을 등록합니다. (이메일 중복 시 분산 락으로 동시 요청 방어)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "이메일 중복 또는 유효성 검사 실패")
    })
    ResponseEntity<CommonResDto<com.kindtail.adoptmate.member.dto.MemberResponseDto>> registerMember(@RequestBody @Valid MemberRegisterRequestDto requestDto);

    @Operation(summary = "일반 로그인", description = "이메일과 비밀번호로 로그인하여 Access Token 및 Refresh Token을 발급받습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "비밀번호 불일치 또는 존재하지 않는 회원")
    })
    ResponseEntity<CommonResDto<com.kindtail.adoptmate.member.dto.MemberLoginResultDto>> login(@RequestBody @Valid MemberLoginRequestDto dto);

    @Operation(summary = "Access Token 재발급", description = "Redis에 저장된 유효한 Refresh Token으로 새로운 Access Token을 발급받습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 Refresh Token")
    })
    ResponseEntity<CommonResDto<Map<String, Object>>> refreshToken(@RequestBody Map<String, String> request);

    @Operation(summary = "로그아웃", description = "Redis에서 Refresh Token을 삭제하고, 현재 Access Token을 Blacklist에 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공")
    })
    ResponseEntity<CommonResDto<Void>> logout(HttpServletRequest request);

    @Operation(summary = "내 프로필 정보 조회", description = "현재 로그인된 회원의 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "내 정보 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    ResponseEntity<CommonResDto<com.kindtail.adoptmate.member.dto.MemberInfoResponseDto>> getMyInfo();

    @Operation(summary = "전체 회원 목록 조회 (관리자 전용)", description = "관리자 권한으로 시스템의 전체 회원 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "전체 회원 조회 성공"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    ResponseEntity<CommonResDto<java.util.List<com.kindtail.adoptmate.member.dto.MemberInfoResponseDto>>> getAllMembers();

    @Operation(summary = "로그인 상태 비밀번호 변경", description = "기존 비밀번호 검증 후 새로운 비밀번호로 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "비밀번호 변경 완료"),
            @ApiResponse(responseCode = "400", description = "현재 비밀번호 불일치")
    })
    ResponseEntity<CommonResDto<Void>> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid PasswordChangeRequestDto dto
    );

    @Operation(summary = "회원 탈퇴", description = "회원 탈퇴를 처리하고 기존 토큰을 무효화(Blacklist)하며 Soft Delete를 수행합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원 탈퇴 완료"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    ResponseEntity<CommonResDto<Void>> deleteMember(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request
    );

    @Operation(summary = "관리자 회원 강제 삭제 (관리자 전용)", description = "관리자 권한으로 특정 회원을 강제 탈퇴(Soft Delete) 처리하고 리프레시 토큰 및 캐시를 무효화합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "관리자에 의해 회원이 삭제되었습니다."),
            @ApiResponse(responseCode = "400", description = "관리자 본인 계정 삭제 시도 또는 유효하지 않은 요청"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음 또는 다른 관리자 계정 삭제 시도"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회원")
    })
    ResponseEntity<CommonResDto<Void>> deleteMemberByAdmin(
            @Parameter(description = "삭제할 회원 ID", required = true) Long memberId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    );
}
