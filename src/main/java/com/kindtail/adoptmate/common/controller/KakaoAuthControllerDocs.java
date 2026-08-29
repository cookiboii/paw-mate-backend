package com.kindtail.adoptmate.common.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

@Tag(name = "3. 카카오 소셜 로그인 API", description = "카카오 OAuth2 인가 코드 수신 및 로그인/토큰 발급 콜백 API")
public interface KakaoAuthControllerDocs {

    @Operation(summary = "카카오 OAuth2 콜백", description = "카카오 인증 서버로부터 인가 코드(code)를 받아 JWT 토큰을 발급하고 프론트엔드로 전달합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "카카오 로그인 및 토큰 발급 완료 (HTML postMessage 응답)"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 인가 코드")
    })
    void kakaoCallback(
            @Parameter(description = "카카오 인가 코드", example = "authorization_code_here") @RequestParam String code,
            HttpServletResponse response
    ) throws IOException;
}
