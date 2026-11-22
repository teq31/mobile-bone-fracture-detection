package com.bonefracture.auth;

import com.bonefracture.persistence.UserEntity;
import com.bonefracture.persistence.UserRepository;
import com.bonefracture.user.Doctor;
import com.bonefracture.user.Patient;
import com.bonefracture.user.Role;
import com.bonefracture.user.Student;
import com.bonefracture.user.UserNotFoundException;
import com.bonefracture.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Ianis's UserService interface, backed by the database: the factories (FACTORY METHOD)
 * create the right type of user and the account is saved with a BCrypt password.
 */
@SpringBootTest
@Transactional
class DatabaseUserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void theBackendUsesTheDatabaseImplementation() {
        assertInstanceOf(DatabaseUserService.class, userService);
    }

    @Test
    void factoryForTheRoleCreatesTheRightTypeOfUser() {
        assertInstanceOf(Doctor.class, userService.register(Role.DOCTOR, "Dan", "dan@mail.com", "pass1234"));
        assertInstanceOf(Student.class, userService.register(Role.STUDENT, "Ana", "ana@mail.com", "pass1234"));
        assertInstanceOf(Patient.class, userService.register(Role.PATIENT, "Ion", "ion@mail.com", "pass1234"));
    }

    @Test
    void registeredUserIsSavedWithRoleAndHashedPassword() {
        userService.register(Role.DOCTOR, "Dan", "dan@mail.com", "pass1234");

        UserEntity saved = userRepository.findByEmail("dan@mail.com").orElseThrow();
        assertEquals(Role.DOCTOR, saved.getRole());
        assertFalse(saved.isVerified());
        assertNotEquals("pass1234", saved.getPasswordHash());
    }

    @Test
    void sameEmailCannotBeUsedTwice() {
        userService.register(Role.PATIENT, "Ion", "ion@mail.com", "pass1234");

        assertThrows(EmailAlreadyUsedException.class,
                () -> userService.register(Role.STUDENT, "Ion", "ion@mail.com", "other123"));
    }

    @Test
    void loginChecksThePasswordSavedInTheDatabase() {
        userService.register(Role.PATIENT, "Ion", "ion@mail.com", "pass1234");

        assertTrue(userService.login("ion@mail.com", "pass1234"));
        assertFalse(userService.login("ion@mail.com", "wrong"));
        assertFalse(userService.login("nobody@mail.com", "pass1234"));
    }

    @Test
    void verifyDoctorIsSaved() {
        userService.register(Role.DOCTOR, "Dan", "dan@mail.com", "pass1234");
        userService.verifyDoctor("dan@mail.com");
        assertTrue(userRepository.findByEmail("dan@mail.com").orElseThrow().isVerified());
    }

    @Test
    void onlyDoctorsCanBeVerified() {
        userService.register(Role.PATIENT, "Ion", "ion@mail.com", "pass1234");
        assertThrows(IllegalArgumentException.class, () -> userService.verifyDoctor("ion@mail.com"));
        assertFalse(userRepository.findByEmail("ion@mail.com").orElseThrow().isVerified());
    }

    @Test
    void verifyingAnUnknownEmailFails() {
        assertThrows(UserNotFoundException.class, () -> userService.verifyDoctor("nobody@mail.com"));
    }
}
