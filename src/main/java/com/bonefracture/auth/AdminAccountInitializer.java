package com.bonefracture.auth;

import com.bonefracture.user.UserService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

public class AdminAccountInitializer implements ApplicationRunner {

    public AdminAccountInitializer(UserService userService, String email, String password) {
    }

    @Override
    public void run(ApplicationArguments args) {
    }
}
