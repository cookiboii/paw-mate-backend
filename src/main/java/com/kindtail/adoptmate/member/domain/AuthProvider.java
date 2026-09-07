package com.kindtail.adoptmate.member.domain;

/**
 * 로그인/회원 인증 제공자 구분
 * - EMAIL: 자체 이메일 가입 및 로그인
 * - KAKAO: 카카오 OAuth2 소셜 로그인
 * - GOOGLE: 구글 OAuth2 소셜 로그인
 */
public enum AuthProvider {
    EMAIL,
    KAKAO,
    GOOGLE
}
