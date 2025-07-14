package com.kindtail.adoptmate.common.controller;

import com.kindtail.adoptmate.common.service.EmailVerificationService;
import com.kindtail.adoptmate.member.dto.MemberLoginResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/adoptmate")
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    // 1. 이메일 인증 코드 발송
    @PostMapping("/verify-email")
    public ResponseEntity<?> sendVerificationEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body("이메일이 비어있습니다.");
        }

        try {
            emailVerificationService.mailCheck(email);
            return ResponseEntity.ok("인증 코드가 이메일로 전송되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 2. 인증 코드 확인
    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> request) {
        try {
            emailVerificationService.verifyEmail(request);
            return ResponseEntity.ok("이메일 인증 완료!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 3. 비밀번호 변경
    @PatchMapping("/password")
    public ResponseEntity<?> updatePassword(@RequestBody MemberLoginResponseDto dto) {
        try {
            emailVerificationService.updatePassword(dto);
            return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
