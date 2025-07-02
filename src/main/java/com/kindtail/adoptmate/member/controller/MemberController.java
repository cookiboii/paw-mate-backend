package com.kindtail.adoptmate.member.controller;

import com.kindtail.adoptmate.auth.JwtTokenProvider;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.dto.MemberLoginResponseDto;
import com.kindtail.adoptmate.member.dto.MemberRegisterRequestDto;
import com.kindtail.adoptmate.member.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/adoptmate")
public class MemberController {

    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    public MemberController(MemberService memberService, JwtTokenProvider jwtTokenProvider) {
        this.memberService = memberService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerMember(@RequestBody @Valid MemberRegisterRequestDto memberRegisterRequestDto) {
      Member member = memberService.registerMember(memberRegisterRequestDto);
      return ResponseEntity.ok(member);

    }
/*    @PostMapping("/login")
    public ResponseEntity<?> login(MemberLoginResponseDto memberLoginResponseDto) {
        Member member =memberService.authenticateMember(memberLoginResponseDto);
        String token
                 = jwtTokenProvider.createToken(member.getEmail() , member.getRole().toString());
        Map<String, String> map = new HashMap<>();
        map.put("token", token);
        map.put("email", member.getEmail());

        return ResponseEntity.of();
    }*/
}
