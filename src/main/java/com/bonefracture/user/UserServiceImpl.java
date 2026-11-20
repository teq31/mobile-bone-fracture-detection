package com.bonefracture.user;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class UserServiceImpl implements UserService {

    private Map<Role, UserFactory> factories = new EnumMap<>(Role.class);
    private Map<String, User> users = new HashMap<>();

    public UserServiceImpl() {
        factories.put(Role.STUDENT, new StudentFactory());
        factories.put(Role.DOCTOR, new DoctorFactory());
        factories.put(Role.PATIENT, new PatientFactory());
        factories.put(Role.ADMINISTRATOR, new AdministratorFactory());
    }

    @Override
    public User register(Role role, String name, String email, String password) {
        if (users.containsKey(email)) {
            throw new IllegalArgumentException("email already used: " + email);
        }
        User user = factories.get(role).register(name, email, password);
        users.put(email, user);
        return user;
    }

    @Override
    public boolean login(String email, String password) {
        User user = users.get(email);
        return user != null && user.login(password);
    }
}
