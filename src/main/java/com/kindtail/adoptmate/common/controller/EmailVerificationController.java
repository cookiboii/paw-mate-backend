package com.kindtail.adoptmate.common.controller;

import com.kindtail.adoptmate.common.dto.CommonResDto;
import com.kindtail.adoptmate.common.dto.EmailSendRequestDto;
import com.kindtail.adoptmate.common.dto.EmailVerifyRequestDto;
import com.kindtail.adoptmate.common.dto.SuccessCode;
import com.kindtail.adoptmate.common.exception.CustomException;
import com.kindtail.adoptmate.common.exception.ErrorCode;
import com.kindtail.adoptmate.common.service.EmailVerificationService;
import com.kindtail.adoptmate.member.dto.PasswordResetRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/adoptmate")
@Validated
public class EmailVerificationController implements EmailVerificationControllerDocs {

    private final EmailVerificationService emailVerificationService;

    @Override
    @PostMapping("/verify-email")
    public ResponseEntity<CommonResDto<Void>> sendVerificationEmail(@Valid @RequestBody EmailSendRequestDto request) {
        emailVerificationService.mailCheck(request.email());
        return CommonResDto.toResponseEntity(SuccessCode.EMAIL_SEND_SUCCESS);
    }

    @Override
    @PostMapping("/verify-code")
    public ResponseEntity<CommonResDto<Map<String, String>>> verifyCode(@Valid @RequestBody EmailVerifyRequestDto request) {
        emailVerificationService.verifyEmail(request.email(), request.code());
        Map<String, String> result = Map.of("email", request.email(), "code", request.code());
        return CommonResDto.toResponseEntity(SuccessCode.EMAIL_VERIFY_SUCCESS, result);
    }

    @Override
    @PostMapping("/send-reset-code")
    public ResponseEntity<CommonResDto<Void>> sendResetCode(@RequestParam @NotBlank @Email String email) {
        emailVerificationService.sendPasswordResetEmail(email);
        return CommonResDto.toResponseEntity(SuccessCode.RESET_CODE_SEND_SUCCESS);
    }

    @Override
    @PostMapping("/verify-reset-code")
    public ResponseEntity<CommonResDto<Void>> verifyResetCode(
            @RequestParam @NotBlank @Email String email,
            @RequestParam @NotBlank String code
    ) {
        boolean verified = emailVerificationService.verifyPassword(email, code);
        if (verified) {
            return CommonResDto.toResponseEntity(SuccessCode.RESET_CODE_VERIFY_SUCCESS);
        } else {
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_CODE_MISMATCH);
        }
    }

    @Override
    @PatchMapping("/password")
    public ResponseEntity<CommonResDto<Void>> updatePassword(@RequestBody @Valid PasswordResetRequestDto dto) {
        emailVerificationService.updatePassword(dto);
        return CommonResDto.toResponseEntity(SuccessCode.PASSWORD_RESET_SUCCESS);
    }
}
