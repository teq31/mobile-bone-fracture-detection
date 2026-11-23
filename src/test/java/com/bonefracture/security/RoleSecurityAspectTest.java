package com.bonefracture.security;

import com.bonefracture.user.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RoleSecurityAspectTest {

    static class Guarded {
        @RequiresRole(Role.DOCTOR)
        String doctorOnly() {
            return "ok";
        }
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(String authority) {
        var auth = new UsernamePasswordAuthenticationToken(
                "user", "n/a", List.of(new SimpleGrantedAuthority(authority)));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void allowsMatchingRole() {
        authenticateAs("ROLE_DOCTOR");
        assertDoesNotThrow(() -> new Guarded().doctorOnly());
    }

    @Test
    void deniesWrongRole() {
        authenticateAs("ROLE_PATIENT");
        assertThrows(AccessDeniedException.class, () -> new Guarded().doctorOnly());
    }

    @Test
    void deniesWhenNotAuthenticated() {
        SecurityContextHolder.clearContext();
        assertThrows(AccessDeniedException.class, () -> new Guarded().doctorOnly());
    }
}
