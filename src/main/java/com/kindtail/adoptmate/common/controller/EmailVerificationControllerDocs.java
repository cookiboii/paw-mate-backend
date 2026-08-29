package com.kindtail.adoptmate.common.controller;

import com.kindtail.adoptmate.common.dto.CommonResDto;
import com.kindtail.adoptmate.member.dto.PasswordResetRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Tag(name = "2. 이메일 인증 & 비밀번호 재설정 API", description = "회원가입 이메일 인증 코드 발송/검증 및 비밀번호 재설정 관련 API")
public interface EmailVerificationControllerDocs {

    @Operation(summary = "회원가입 이메일 인증 코드 발송", description = "회원가입 시 본인 확인을 위한 6자리 인증 코드를 이메일로 전송합니다. (유효시간 3분)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "인증 코드 전송 완료"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 이메일")
    })
    ResponseEntity<CommonResDto> sendVerificationEmail(@RequestBody Map<String, String> request);

    @Operation(summary = "회원가입 이메일 인증 코드 확인", description = "전송받은 6자리 인증 코드를 검증합니다. (5회 연속 실패 시 30분간 차단)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "이메일 인증 성공"),
            @ApiResponse(responseCode = "400", description = "인증 코드 불일치 또는 만료")
    })
    ResponseEntity<CommonResDto> verifyCode(@RequestBody Map<String, String> request);

    @Operation(summary = "비밀번호 재설정 인증 코드 발송", description = "가입된 이메일 계정으로 비밀번호 재설정용 인증 코드를 발송합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재설정 인증 코드 전송 완료"),
            @ApiResponse(responseCode = "404", description = "가입되지 않은 이메일")
    })
    ResponseEntity<CommonResDto> sendResetCode(
            @Parameter(description = "가입된 이메일 주소", example = "user@example.com") @RequestParam String email
    );

    @Operation(summary = "비밀번호 재설정 인증 코드 확인", description = "비밀번호 재설정 인증 코드를 확인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "인증 성공"),
            @ApiResponse(responseCode = "400", description = "인증 코드 불일치 또는 만료")
    })
    ResponseEntity<CommonResDto> verifyResetCode(
            @Parameter(description = "이메일 주소", example = "user@example.com") @RequestParam String email,
            @Parameter(description = "이메일로 수신한 인증 코드", example = "123456") @RequestParam String code
    );

    @Operation(summary = "비밀번호 재설정 실행", description = "이메일 인증이 완료된 후 새로운 비밀번호를 설정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "비밀번호 재설정 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 요청 또는 비밀번호 규칙 위반")
    })
    ResponseEntity<CommonResDto> updatePassword(@RequestBody @Valid PasswordResetRequestDto dto);
}
