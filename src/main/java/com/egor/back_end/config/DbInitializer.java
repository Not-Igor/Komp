package com.egor.back_end.config;

import com.egor.back_end.model.*;
import com.egor.back_end.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DbInitializer {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public DbInitializer(PasswordEncoder passwordEncoder,
                         UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    public void clearAll() {
        userRepository.deleteAll();
    }

    @PostConstruct
    public void init() {
        userRepository.deleteAll();


        final var admin = userRepository.save(new User(

                "a",
                passwordEncoder.encode("a"),
                "admin@egor.be",
                Role.ADMIN));


    }

}
