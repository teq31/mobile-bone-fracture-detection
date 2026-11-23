package com.bonefracture.audit;

import com.bonefracture.user.Role;
import com.bonefracture.user.UserNotFoundException;
import com.bonefracture.user.UserServiceImpl;
import org.aspectj.lang.Aspects;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuditLoggingAspectTest {

    private AuditLog log;
    private UserServiceImpl users;

    @BeforeEach
    void setUp() {
        log = Aspects.aspectOf(AuditLoggingAspect.class).getAuditLog();
        log.clear();
        users = new UserServiceImpl();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private AuditEntry last() {
        List<AuditEntry> entries = log.entries();
        assertFalse(entries.isEmpty(), "nothing was recorded");
        return entries.get(0);
    }

    @Test
    void registerIsRecorded() {
        users.register(Role.DOCTOR, "Dan", "dan@clinic.com", "pass1234");
        AuditEntry entry = last();
        assertEquals("dan@clinic.com", entry.actor());
        assertEquals("REGISTER", entry.action());
        assertEquals("DOCTOR", entry.target());
        assertTrue(entry.success());
    }

    @Test
    void loginIsRecordedWithItsResult() {
        users.register(Role.PATIENT, "Ion", "ion@mail.com", "pass1234");
        users.login("ion@mail.com", "pass1234");
        assertEquals("LOGIN", last().action());
        assertTrue(last().success());

        users.login("ion@mail.com", "wrong");
        assertEquals("ion@mail.com", last().actor());
        assertFalse(last().success());
    }

    @Test
    void passwordsAreNeverRecorded() {
        users.register(Role.PATIENT, "Ion", "ion@mail.com", "secret-pass");
        users.login("ion@mail.com", "secret-pass");
        assertTrue(log.entries().stream().noneMatch(e -> e.toString().contains("secret-pass")));
    }

    @Test
    void doctorVerificationRecordsTheAdministrator() {
        users.register(Role.DOCTOR, "Dan", "dan@clinic.com", "pass1234");
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("admin@mail.com", null, "ROLE_ADMINISTRATOR"));

        users.verifyDoctor("dan@clinic.com");

        AuditEntry entry = last();
        assertEquals("admin@mail.com", entry.actor());
        assertEquals("VERIFY_DOCTOR", entry.action());
        assertEquals("dan@clinic.com", entry.target());
        assertTrue(entry.success());
    }

    @Test
    void failedActionIsRecordedAndTheErrorStillThrown() {
        assertThrows(UserNotFoundException.class, () -> users.verifyDoctor("nobody@mail.com"));
        AuditEntry entry = last();
        assertEquals("VERIFY_DOCTOR", entry.action());
        assertEquals("system", entry.actor());
        assertFalse(entry.success());
    }

    @Test
    void newestEntryComesFirst() {
        users.register(Role.PATIENT, "Ion", "ion@mail.com", "pass1234");
        users.login("ion@mail.com", "pass1234");
        assertEquals(List.of("LOGIN", "REGISTER"), log.entries().stream().map(AuditEntry::action).toList());
    }
}
