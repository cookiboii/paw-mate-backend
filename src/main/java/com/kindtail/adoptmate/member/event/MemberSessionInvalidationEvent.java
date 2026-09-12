package com.kindtail.adoptmate.member.event;

/** Published by a successful member-deletion transaction. */
public record MemberSessionInvalidationEvent(String email, String accessToken) {
}
