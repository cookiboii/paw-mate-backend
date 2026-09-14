package com.kindtail.adoptmate.auth;

import com.kindtail.adoptmate.common.exception.CustomException;
import com.kindtail.adoptmate.common.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Provides the authenticated application user to domain services. */
@Component
public class CurrentUserProvider {

    public CustomUserDetails currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails;
        }
        throw new CustomException(ErrorCode.UNAUTHORIZED);
    }

    public Long currentUserId() {
        return currentUser().getId();
    }

    public String currentUserEmail() {
        return currentUser().getEmail();
    }

    public Optional<Long> optionalCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return Optional.ofNullable(userDetails.getId());
        }
        return Optional.empty();
    }
}
