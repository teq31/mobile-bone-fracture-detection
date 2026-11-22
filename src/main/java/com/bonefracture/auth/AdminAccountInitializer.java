package com.bonefracture.auth;

import com.bonefracture.user.Role;
import com.bonefracture.user.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

/**
 * Administrators cannot register through the API, so the first one is created at startup
 * from ADMIN_EMAIL / ADMIN_PASSWORD. Without them no administrator is created.
 */
@Component
public class AdminAccountInitializer implements ApplicationRunner {

    private static final Logger LOGGER = Logger.getLogger(AdminAccountInitializer.class.getName());

    private final UserService userService;
    private final String email;
    private final String password;

    public AdminAccountInitializer(UserService userService,
                                   @Value("${app.admin.email:}") String email,
                                   @Value("${app.admin.password:}") String password) {
        this.userService = userService;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return;
        }
        try {
            userService.register(Role.ADMINISTRATOR, "Administrator", email, password);
            LOGGER.info("administrator account created: " + email);
        } catch (EmailAlreadyUsedException | IllegalArgumentException e) {
            // already created at an earlier start
        }
    }
}
