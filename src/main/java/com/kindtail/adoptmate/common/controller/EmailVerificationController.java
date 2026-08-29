package com.kindtail.adoptmate.common.controller;

import com.kindtail.adoptmate.common.dto.CommonResDto;
import com.kindtail.adoptmate.common.service.EmailVerificationService;
import com.kindtail.adoptmate.member.dto.PasswordResetRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/adoptmate")
public class EmailVerificationController implements EmailVerificationControllerDocs {

    private final EmailVerificationService emailVerificationService;

    @Override
    @PostMapping("/verify-email")
    public ResponseEntity<CommonResDto> sendVerificationEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("이메일이 비어있습니다.");
        }
        emailVerificationService.mailCheck(email);
        return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "인증 코드가 이메일로 전송되었습니다.", null));
    }

    @Override
    @PostMapping("/verify-code")
    public ResponseEntity<CommonResDto> verifyCode(@RequestBody Map<String, String> request) {
        Map<String, String> result = emailVerificationService.verifyEmail(request);
        return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "이메일 인증 완료!", result));
    }

    @Override
    @PostMapping("/send-reset-code")
    public ResponseEntity<CommonResDto> sendResetCode(@RequestParam String email) {
        emailVerificationService.sendPasswordResetEmail(email);
        return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "인증 코드가 이메일로 전송되었습니다.", null));
    }

    @Override
    @PostMapping("/verify-reset-code")
    public ResponseEntity<CommonResDto> verifyResetCode(@RequestParam String email, @RequestParam String code) {
        boolean verified = emailVerificationService.verifyPassword(email, code);
        if (verified) {
            return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "인증 성공", null));
        } else {
            throw new IllegalArgumentException("인증 실패: 잘못된 코드이거나 만료되었습니다.");
        }
    }

    @Override
    @PatchMapping("/password")
    public ResponseEntity<CommonResDto> updatePassword(@RequestBody @Valid PasswordResetRequestDto dto) {
        emailVerificationService.updatePassword(dto);
        return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "비밀번호가 성공적으로 변경되었습니다.", null));
    }
}
