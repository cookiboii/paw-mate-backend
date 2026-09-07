package com.kindtail.adoptmate.member.repository;

import com.kindtail.adoptmate.member.domain.AuthProvider;
import com.kindtail.adoptmate.member.domain.Member;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);

    Optional<Member> findByAuthProviderAndSocialId(AuthProvider authProvider, String socialId);

    default Optional<Member> findBySocialProviderAndSocialId(String socialProvider, String socialId) {
        if (socialProvider == null) {
            return Optional.empty();
        }
        try {
            return findByAuthProviderAndSocialId(AuthProvider.valueOf(socialProvider.toUpperCase()), socialId);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
