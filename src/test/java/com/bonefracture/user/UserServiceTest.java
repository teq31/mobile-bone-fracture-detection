package com.bonefracture.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserServiceImpl();
    }

    @Test
    void registerUsesTheRole() {
        assertInstanceOf(Doctor.class, service.register(Role.DOCTOR, "Dan", "dan@mail.com", "pass123"));
        assertInstanceOf(Student.class, service.register(Role.STUDENT, "Ana", "ana@mail.com", "pass123"));
    }

    @Test
    void sameEmailCannotBeUsedTwice() {
        service.register(Role.PATIENT, "Ion", "ion@mail.com", "pass123");
        assertThrows(IllegalArgumentException.class,
                () -> service.register(Role.STUDENT, "Ion", "ion@mail.com", "other"));
    }

    @Test
    void login() {
        service.register(Role.PATIENT, "Ion", "ion@mail.com", "pass123");
        assertTrue(service.login("ion@mail.com", "pass123"));
        assertFalse(service.login("ion@mail.com", "wrong"));
        assertFalse(service.login("nobody@mail.com", "pass123"));
    }
}
