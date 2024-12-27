package org.smg.google.home.service;

import jakarta.transaction.Transactional;
import org.smg.google.home.exception.UserNotFoundException;
import org.smg.google.home.model.Role;
import org.smg.google.home.model.User;
import org.smg.google.home.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User saveUser(String email, String name, Role role) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setRole(role);

        return userRepository.save(user);
    }

    public User getUserByGoogleId(String googleId) {
        return userRepository.findByGoogleId(googleId)
                .orElseThrow(() -> new UserNotFoundException("User not found with given Google ID"));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with given email: " + email));
    }

    public boolean checkIfUserExists(String email) {
        return userRepository.existsByEmail(email);
    }
}
