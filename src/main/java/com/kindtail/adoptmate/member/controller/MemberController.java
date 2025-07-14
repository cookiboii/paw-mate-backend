package com.kindtail.adoptmate.member.controller;

import com.kindtail.adoptmate.auth.JwtTokenProvider;
import com.kindtail.adoptmate.auth.TokenUserInfo;
import com.kindtail.adoptmate.common.dto.CommonResDto;
import com.kindtail.adoptmate.common.service.EmailVerificationService;
import com.kindtail.adoptmate.common.service.MailSenderService;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.dto.*;
import com.kindtail.adoptmate.member.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/adoptmate")
public class MemberController {

    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailVerificationService emailVerificationService;
    private final MailSenderService mailSenderService;

    public MemberController(MemberService memberService, JwtTokenProvider jwtTokenProvider,
                            EmailVerificationService emailVerificationService, MailSenderService mailSenderService) {
        this.memberService = memberService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.emailVerificationService = emailVerificationService;
        this.mailSenderService = mailSenderService;
    }

    @PostMapping("/register")
    public ResponseEntity<CommonResDto> registerMember(@RequestBody @Valid MemberRegisterRequestDto requestDto) {
        Member member = memberService.registerMember(requestDto);
        MemberResponseDto responseDto = MemberResponseDto.from(member);
        return ResponseEntity.status(CREATED).body(new CommonResDto(CREATED, "회원가입 성공", responseDto));
    }

    @PostMapping("/login")
    public ResponseEntity<CommonResDto> login(@RequestBody MemberLoginResponseDto dto) {
        Member member = memberService.authenticateMember(dto);
        String token = jwtTokenProvider.createToken(member.getEmail(), member.getRole().toString());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("email", member.getEmail());
        result.put("role", member.getRole().toString());

        return ResponseEntity.ok(new CommonResDto(OK, "Login Success", result));
    }

    @GetMapping("/myInfo")
    public ResponseEntity<MemberInfoRequestDto> getMyInfo() {
        Member member = memberService.MemberInfo();
        MemberInfoRequestDto dto = MemberInfoRequestDto.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .role(member.getRole())
                .build();
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResDto> getAllMembers() {
        List<Member> members = memberService.getMembers();
        List<MemberInfoRequestDto> dtoList = members.stream()
                .map(member -> MemberInfoRequestDto.builder()
                        .id(member.getId())
                        .name(member.getName())
                        .email(member.getEmail())
                        .role(member.getRole())
                        .build())
                .toList();
        return ResponseEntity.ok(new CommonResDto(OK, "전체조회", dtoList));
    }

    @PostMapping("/password")
    public ResponseEntity<CommonResDto> changePassword(@RequestBody PasswordChangeRequestDto dto) {
        TokenUserInfo userInfo = (TokenUserInfo) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        memberService.changePassword(userInfo.getEmail(), dto);
        return ResponseEntity.ok(new CommonResDto(OK, "비밀번호 변경 완료", dto));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<CommonResDto> deleteMember() {
        TokenUserInfo userInfo = (TokenUserInfo) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        memberService.deleteUser(userInfo.getEmail());
        return ResponseEntity.ok(new CommonResDto(OK, "회원 탈퇴 완료", userInfo));
    }





}
