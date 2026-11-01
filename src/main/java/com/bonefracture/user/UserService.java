package com.bonefracture.user;

public interface UserService {

    User register(Role role, String name, String email, String password);

    boolean login(String email, String password);
}
