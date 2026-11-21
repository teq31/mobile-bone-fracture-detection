package com.bonefracture.security;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@Aspect
public class RoleSecurityAspect {

    @Before("@annotation(requiresRole)")
    public void checkRole(RequiresRole requiresRole) {
        String required = "ROLE_" + requiresRole.value().name();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("authentication required");
        }
        boolean allowed = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(required::equals);
        if (!allowed) {
            throw new AccessDeniedException("requires role " + requiresRole.value().name());
        }
    }
}
