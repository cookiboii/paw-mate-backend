package com.kindtail.adoptmate.member.service;

import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.dto.MemberLoginResponseDto;
import com.kindtail.adoptmate.member.dto.MemberRegisterRequestDto;
import com.kindtail.adoptmate.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
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
        String address = memberRegisterRequestDto.address();

        password = passwordEncoder.encode(password);
  Optional<Member> findmember = memberRepository.findByEmail(email);
  if (findmember.isPresent()) {
      throw new IllegalArgumentException("Email already exists");

  }

        Member member = Member.builder().email(email).name(username).password(password).address(address).build();
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

}
