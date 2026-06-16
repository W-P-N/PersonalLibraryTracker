package com.wpn.personallibrarytracker.repository;

import com.wpn.personallibrarytracker.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

@DataJpaTest
public class UserRepositoryTest {
    @Autowired
    UserRepository userRepository;

    @Test
    void findByEmail_shouldReturnUser_whenEmailExists() {
        User existingUser = new User();
        existingUser.setUserName("test");
        existingUser.setEmail("test@mail.com");
        existingUser.setPassword("testPassword");
        userRepository.save(existingUser);

        Optional<User> userOptional = userRepository.findByEmail("test@mail.com");
        User foundUser = null;
        if (userOptional.isPresent()) {
            foundUser = userOptional.get();
        }
        Assertions.assertTrue(userOptional.isPresent());
        Assertions.assertEquals(foundUser.getEmail(), existingUser.getEmail());
    }

    @Test
    void findByEmail_shouldReturnEmpty_whenEmailNotExists() {
        Optional<User> foundUser = userRepository.findByEmail("test@gmail.com");
        Assertions.assertFalse(foundUser.isPresent());
    }
}
