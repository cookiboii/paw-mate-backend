package com.kindtail.adoptmate.member.service;

import com.kindtail.adoptmate.auth.TokenUserInfo;
import com.kindtail.adoptmate.common.service.MailSenderService;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.dto.MemberLoginResponseDto;
import com.kindtail.adoptmate.member.dto.MemberRegisterRequestDto;
import com.kindtail.adoptmate.member.dto.PasswordChangeRequestDto;
import com.kindtail.adoptmate.member.repository.MemberRepository;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Optional;


@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberService(MemberRepository memberRepository , PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;

    }

    @Transactional
    public Member registerMember(MemberRegisterRequestDto memberRegisterRequestDto) {
        String email = memberRegisterRequestDto.email();
        String password = memberRegisterRequestDto.password();
        String username = memberRegisterRequestDto.name();


        password = passwordEncoder.encode(password);
  Optional<Member> findMember = memberRepository.findByEmail(email);
  if (findMember.isPresent()) {
      throw new IllegalArgumentException("Email already exists");

  }

        Member member = Member.builder().email(email).name(username).password(password).build();
        return memberRepository.save(member);
    }
    @Transactional
     public Member authenticateMember (MemberLoginResponseDto loginResponseDto ) {

          String email = loginResponseDto.email();
          String password = loginResponseDto.password();

         Member member = memberRepository.findByEmail(email).orElseThrow(()->
                new EntityNotFoundException("Member not found"));

          if (!passwordEncoder.matches(password, member.getPassword())) {
              throw new IllegalArgumentException("Wrong password");
          }

         return member;
    }

    @Transactional(readOnly = true)
    public Member MemberInfo() {
        TokenUserInfo userInfo = (TokenUserInfo) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        Member member = memberRepository.findByEmail(userInfo.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("User not found!"));
        return member;
    }
   @Transactional
   public List<Member> getMembers() {

        return memberRepository.findAll();
   }



   @Transactional
    public void deleteUser(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("회원이 존재하지 않습니다."));
        memberRepository.delete(member);
    }
   @Transactional
    public void changePassword(String email, PasswordChangeRequestDto dto) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("회원이 존재하지않습니다."));
        if (!passwordEncoder.matches(dto.currentPassword(), member.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }
        String encodedNewPassword = passwordEncoder.encode(dto.newPassword());
       member.updatePassword(encodedNewPassword);
    }
   @Transactional
    public Long getMemberIdByEmail(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일의 사용자가 존재하지 않습니다."));
        return member.getId();
    }



}
