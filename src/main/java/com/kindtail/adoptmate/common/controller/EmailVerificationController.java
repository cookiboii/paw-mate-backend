package com.kindtail.adoptmate.common.controller;

import com.kindtail.adoptmate.common.dto.CommonResponse;
import com.kindtail.adoptmate.common.dto.EmailSendRequest;
import com.kindtail.adoptmate.common.dto.EmailVerifyRequest;
import com.kindtail.adoptmate.common.dto.SuccessCode;
import com.kindtail.adoptmate.common.exception.CustomException;
import com.kindtail.adoptmate.common.exception.ErrorCode;
import com.kindtail.adoptmate.common.service.EmailVerificationService;
import com.kindtail.adoptmate.member.dto.PasswordResetRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/adoptmate")
@Validated
public class EmailVerificationController implements EmailVerificationControllerDocs {

    private final EmailVerificationService emailVerificationService;

    @Override
    @PostMapping("/verify-email")
    public ResponseEntity<CommonResponse<Void>> sendVerificationEmail(@Valid @RequestBody EmailSendRequest request) {
        emailVerificationService.mailCheck(request.email());
        return CommonResponse.toResponseEntity(SuccessCode.EMAIL_SEND_SUCCESS);
    }

    @Override
    @PostMapping("/verify-code")
    public ResponseEntity<CommonResponse<Map<String, String>>> verifyCode(@Valid @RequestBody EmailVerifyRequest request) {
        emailVerificationService.verifyEmail(request.email(), request.code());
        Map<String, String> result = Map.of("email", request.email(), "code", request.code());
        return CommonResponse.toResponseEntity(SuccessCode.EMAIL_VERIFY_SUCCESS, result);
    }

    @Override
    @PostMapping("/send-reset-code")
    public ResponseEntity<CommonResponse<Void>> sendResetCode(@RequestParam String email) {
        emailVerificationService.sendPasswordResetEmail(email);
        return CommonResponse.toResponseEntity(SuccessCode.RESET_CODE_SEND_SUCCESS);
    }

    @Override
    @PostMapping("/verify-reset-code")
    public ResponseEntity<CommonResponse<Void>> verifyResetCode(
            @RequestParam String email,
            @RequestParam String code
    ) {
        boolean verified = emailVerificationService.verifyPassword(email, code);
        if (verified) {
            return CommonResponse.toResponseEntity(SuccessCode.RESET_CODE_VERIFY_SUCCESS);
        } else {
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_CODE_MISMATCH);
        }
    }

    @Override
    @PatchMapping("/password")
    public ResponseEntity<CommonResponse<Void>> updatePassword(@RequestBody @Valid PasswordResetRequest dto) {
        emailVerificationService.updatePassword(dto);
        return CommonResponse.toResponseEntity(SuccessCode.PASSWORD_RESET_SUCCESS);
    }
}
