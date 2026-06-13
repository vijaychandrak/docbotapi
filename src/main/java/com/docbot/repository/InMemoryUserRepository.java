package com.docbot.repository;

import com.docbot.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryUserRepository {

    private final Map<String, User> usersByUsername = new ConcurrentHashMap<>();
    private final Map<String, User> usersByEmail = new ConcurrentHashMap<>();

    public InMemoryUserRepository(PasswordEncoder passwordEncoder) {
        // Seed a default user
        User admin = User.builder()
                .username("admin")
                .email("admin@docbot.com")
                .passwordHash(passwordEncoder.encode("admin123"))
                .role("ROLE_ADMIN")
                .build();
        admin.setId(UUID.randomUUID());
        usersByUsername.put(admin.getUsername(), admin);
        usersByEmail.put(admin.getEmail(), admin);
    }

    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(usersByUsername.get(username));
    }

    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(usersByEmail.get(email));
    }

    public Optional<User> findByUsernameOrEmail(String identifier) {
        return findByUsername(identifier)
                .or(() -> findByEmail(identifier));
    }

    public User save(User user) {
        if (user.getId() == null) {
            user.setId(UUID.randomUUID());
        }
        usersByUsername.put(user.getUsername(), user);
        usersByEmail.put(user.getEmail(), user);
        return user;
    }

    public boolean existsByUsername(String username) {
        return usersByUsername.containsKey(username);
    }

    public boolean existsByEmail(String email) {
        return usersByEmail.containsKey(email);
    }
}
