package com.kindtail.adoptmate.member.repository;

import com.kindtail.adoptmate.member.domain.Member;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
 Optional<Member> findByEmail(String email);

    Optional<Member> findBySocialProviderAndSocialId(String socialId, String socialProvider);
}
