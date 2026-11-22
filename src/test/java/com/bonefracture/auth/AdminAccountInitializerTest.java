package com.bonefracture.auth;

import com.bonefracture.user.Administrator;
import com.bonefracture.user.Role;
import com.bonefracture.user.UserServiceImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AdminAccountInitializerTest {

    @Test
    void createsTheAdministratorFromTheSettings() {
        UserServiceImpl users = new UserServiceImpl();
        new AdminAccountInitializer(users, "admin@mail.com", "admin1234").run(null);
        assertTrue(users.login("admin@mail.com", "admin1234"));
    }

    @Test
    void doesNothingWithoutSettings() {
        UserServiceImpl users = new UserServiceImpl();
        new AdminAccountInitializer(users, "", "").run(null);
        assertFalse(users.login("", ""));
    }

    @Test
    void existingAdministratorIsKept() {
        UserServiceImpl users = new UserServiceImpl();
        users.register(Role.ADMINISTRATOR, "Admin", "admin@mail.com", "first1234");
        assertDoesNotThrow(() -> new AdminAccountInitializer(users, "admin@mail.com", "other1234").run(null));
        assertTrue(users.login("admin@mail.com", "first1234"));
    }
}
