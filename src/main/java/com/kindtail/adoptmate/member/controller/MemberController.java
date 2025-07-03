package com.kindtail.adoptmate.member.controller;

import com.kindtail.adoptmate.auth.JwtTokenProvider;
import com.kindtail.adoptmate.common.dto.CommonResDto;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.dto.MemberLoginResponseDto;
import com.kindtail.adoptmate.member.dto.MemberRegisterRequestDto;
import com.kindtail.adoptmate.member.dto.MemberResponseDto;
import com.kindtail.adoptmate.member.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

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
    public ResponseEntity<CommonResDto> registerMember(@RequestBody @Valid MemberRegisterRequestDto requestDto) {
      Member member = memberService.registerMember(requestDto);
        MemberResponseDto responseDto = MemberResponseDto.from(member);

        CommonResDto result = new CommonResDto(CREATED, "회원가입 성공", responseDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);

    }
    @PostMapping("/login")
    public ResponseEntity<CommonResDto> login(@RequestBody MemberLoginResponseDto memberLoginResponseDto) {
        Member member =memberService.authenticateMember(memberLoginResponseDto);
        String token
                 = jwtTokenProvider.createToken(member.getEmail() , member.getRole().toString());
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        map.put("email", member.getEmail());
        map.put("role", member.getRole().toString());
        CommonResDto resDto = new CommonResDto(OK,"Login Success",map);
        return new ResponseEntity<>(resDto, OK);

    }





}
