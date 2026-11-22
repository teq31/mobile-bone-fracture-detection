package com.bonefracture.auth;

import com.bonefracture.persistence.UserEntity;
import com.bonefracture.persistence.UserRepository;
import com.bonefracture.user.AdministratorFactory;
import com.bonefracture.user.Doctor;
import com.bonefracture.user.DoctorFactory;
import com.bonefracture.user.PatientFactory;
import com.bonefracture.user.Role;
import com.bonefracture.user.StudentFactory;
import com.bonefracture.user.User;
import com.bonefracture.user.UserFactory;
import com.bonefracture.user.UserNotFoundException;
import com.bonefracture.user.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.Map;

/**
 * SOA: a second implementation of Ianis's UserService interface, which saves the users in the database.
 * (UserServiceImpl keeps them in memory.)
 *
 * FACTORY METHOD: the factory for the role creates the right type of user (Patient, Doctor, ...);
 * e.g. DoctorFactory creates a Doctor that is not verified yet.
 * The password is stored in the database as a BCrypt hash.
 */
@Service
@Transactional
public class DatabaseUserService implements UserService {

    private final Map<Role, UserFactory> factories = new EnumMap<>(Role.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseUserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        factories.put(Role.STUDENT, new StudentFactory());
        factories.put(Role.DOCTOR, new DoctorFactory());
        factories.put(Role.PATIENT, new PatientFactory());
        factories.put(Role.ADMINISTRATOR, new AdministratorFactory());
    }

    @Override
    public User register(Role role, String name, String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyUsedException(email);
        }
        User user = factories.get(role).register(name, email, password);
        UserEntity entity = new UserEntity(user.getName(), user.getEmail(), passwordEncoder.encode(password), role);
        if (user instanceof Doctor doctor) {
            entity.setVerified(doctor.isVerified());
        }
        userRepository.save(entity);
        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean login(String email, String password) {
        return userRepository.findByEmail(email)
                .map(user -> passwordEncoder.matches(password, user.getPasswordHash()))
                .orElse(false);
    }

    @Override
    public void verifyDoctor(String doctorEmail) {
        UserEntity user = userRepository.findByEmail(doctorEmail)
                .orElseThrow(() -> new UserNotFoundException(doctorEmail));
        if (user.getRole() != Role.DOCTOR) {
            throw new IllegalArgumentException(doctorEmail + " is not a doctor");
        }
        user.setVerified(true);
        userRepository.save(user);
    }
}
