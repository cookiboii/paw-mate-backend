package com.kindtail.adoptmate.member.controller;

import com.kindtail.adoptmate.auth.TokenUserInfo;
import com.kindtail.adoptmate.common.dto.CommonResDto;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.dto.*;
import com.kindtail.adoptmate.member.facade.MemberFacade;
import com.kindtail.adoptmate.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/adoptmate")
@RequiredArgsConstructor
public class MemberController {

    private final MemberFacade memberFacade;
    private final MemberService memberService;

    @PostMapping("/register")
    public ResponseEntity<CommonResDto> registerMember(@RequestBody @Valid MemberRegisterRequestDto requestDto) {
        Member member = memberFacade.registerMember(requestDto);
        MemberResponseDto responseDto = MemberResponseDto.from(member);
        return ResponseEntity.status(CREATED).body(new CommonResDto(CREATED, "회원가입 성공", responseDto));
    }

    @PostMapping("/login")
    public ResponseEntity<CommonResDto> login(@RequestBody @Valid MemberLoginRequestDto dto) {
        MemberLoginResultDto result = memberService.login(dto);
        return ResponseEntity.ok(new CommonResDto(OK, "Login Success", result));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<CommonResDto> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        String newToken = memberService.refreshAccessToken(refreshToken);

        Map<String, Object> result = new HashMap<>();
        result.put("token", newToken);

        return ResponseEntity.ok(new CommonResDto(OK, "토큰 재발급 성공", result));
    }

    @PostMapping("/logout")
    public ResponseEntity<CommonResDto> logout(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String accessToken = bearerToken.substring(7);
            memberService.logout(accessToken);
        }
        return ResponseEntity.ok(new CommonResDto(OK, "로그아웃 성공", null));
    }

    @GetMapping("/myInfo")
    public ResponseEntity<CommonResDto> getMyInfo() {
        Member member = memberService.getMemberInfo();
        MemberInfoResponseDto dto = MemberInfoResponseDto.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .role(member.getRole())
                .build();
        return ResponseEntity.ok(new CommonResDto(OK, "내 정보 조회 성공", dto));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResDto> getAllMembers() {
        List<Member> members = memberService.getMembers();
        List<MemberInfoResponseDto> dtoList = members.stream()
                .map(member -> MemberInfoResponseDto.builder()
                        .id(member.getId())
                        .name(member.getName())
                        .email(member.getEmail())
                        .role(member.getRole())
                        .build())
                .toList();
        return ResponseEntity.ok(new CommonResDto(OK, "전체조회", dtoList));
    }

    @PostMapping("/password")
    public ResponseEntity<CommonResDto> changePassword(
            @AuthenticationPrincipal TokenUserInfo userInfo,
            @RequestBody @Valid PasswordChangeRequestDto dto
    ) {
        String email = userInfo != null ? userInfo.getEmail() :
                ((TokenUserInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail();
        memberService.changePassword(email, dto);
        return ResponseEntity.ok(new CommonResDto(OK, "비밀번호 변경 완료", null));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<CommonResDto> deleteMember(
            @AuthenticationPrincipal TokenUserInfo userInfo,
            HttpServletRequest request
    ) {
        String email = userInfo != null ? userInfo.getEmail() :
                ((TokenUserInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail();
        String accessToken = null;
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            accessToken = bearerToken.substring(7);
        }
        memberService.deleteUser(email, accessToken);
        return ResponseEntity.ok(new CommonResDto(OK, "회원 탈퇴 완료", null));
    }





}
