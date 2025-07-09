package com.kindtail.adoptmate.member.controller;

import com.kindtail.adoptmate.auth.JwtTokenProvider;
import com.kindtail.adoptmate.auth.TokenUserInfo;
import com.kindtail.adoptmate.common.dto.CommonResDto;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.dto.*;
import com.kindtail.adoptmate.member.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
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

  @GetMapping("/myInfo")
  public ResponseEntity<MemberInfoRequestDto> getMyInfo() {
      Member member = memberService.MemberInfo();
      MemberInfoRequestDto dto = MemberInfoRequestDto.builder()
              .id(member.getId())
              .name(member.getName())
              .email(member.getEmail())
              .role(member.getRole())
              .build();

      return new ResponseEntity<>(dto, OK);
  }
  @GetMapping("/all")
 @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<CommonResDto> getAllMembers() {
      List <Member> members = memberService.getMembers();
      List<MemberInfoRequestDto> dtoList = members.stream()
              .map(member -> MemberInfoRequestDto.builder()
                      .id(member.getId()).name(member.getName()).email(member.getEmail()).role(member.getRole()).build())
              .toList();


      return ResponseEntity.ok(new CommonResDto(OK, "전체조회", dtoList));
  }

  @PostMapping("/password")
  public ResponseEntity<CommonResDto> changePassword(@RequestBody PasswordChangeRequestDto dto){
      TokenUserInfo userInfo = (TokenUserInfo) SecurityContextHolder.getContext()
              .getAuthentication().getPrincipal();
         memberService.changePassword(userInfo.getEmail(), dto);

         return ResponseEntity.ok(new CommonResDto(CREATED, "변경완료", dto));


  }
   @DeleteMapping("/delete")
   public ResponseEntity deleteMember( ) {
       TokenUserInfo userInfo = (TokenUserInfo) SecurityContextHolder.getContext()
               .getAuthentication().getPrincipal();
              memberService.deleteUser(userInfo.getEmail());
              return ResponseEntity.ok(new CommonResDto(CREATED, "삭제완료 ", userInfo));
   }


}
