package com.bonefracture.user;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserFactoryTest {

    @Test
    void createsTheRightUserForEachRole() {
        assertInstanceOf(Student.class, new StudentFactory().register("Ana", "ana@mail.com", "pass123"));
        assertInstanceOf(Doctor.class, new DoctorFactory().register("Dan", "dan@mail.com", "pass123"));
        assertInstanceOf(Patient.class, new PatientFactory().register("Ion", "ion@mail.com", "pass123"));
        assertInstanceOf(Administrator.class, new AdministratorFactory().register("Admin", "admin@mail.com", "pass123"));
    }

    @Test
    void keepsNameAndEmail() {
        User user = new StudentFactory().register("Ana", "ana@mail.com", "pass123");
        assertEquals("Ana", user.getName());
        assertEquals("ana@mail.com", user.getEmail());
    }

    @Test
    void passwordIsNotStoredInPlainText() {
        User user = new PatientFactory().register("Ion", "ion@mail.com", "pass123");
        assertNotNull(user.getPasswordHash());
        assertNotEquals("pass123", user.getPasswordHash());
    }

    @Test
    void loginChecksThePassword() {
        User user = new PatientFactory().register("Ion", "ion@mail.com", "pass123");
        assertTrue(user.login("pass123"));
        assertFalse(user.login("wrong"));
    }

    @Test
    void newDoctorIsNotVerified() {
        Doctor doctor = (Doctor) new DoctorFactory().register("Dan", "dan@mail.com", "pass123");
        assertFalse(doctor.isVerified());
    }
}
