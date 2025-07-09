package com.kindtail.adoptmate.member.dto;

public record PasswordChangeRequestDto(
         String currentPassword,
         String newPassword
) {
}
