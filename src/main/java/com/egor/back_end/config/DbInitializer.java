package com.egor.back_end.config;

import com.egor.back_end.model.*;
import com.egor.back_end.repository.FriendRequestRepository;
import com.egor.back_end.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DbInitializer {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final FriendRequestRepository friendRequestRepository;

    public DbInitializer(PasswordEncoder passwordEncoder,
                         UserRepository userRepository,
                         FriendRequestRepository friendRequestRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.friendRequestRepository = friendRequestRepository;
    }

    public void clearAll() {
        friendRequestRepository.deleteAll();
        userRepository.deleteAll();
    }

    @PostConstruct
    public void init() {
        // Delete friend requests first to avoid constraint violations
        friendRequestRepository.deleteAll();
        userRepository.deleteAll();


        final var admin = userRepository.save(new User(

                "a",
                passwordEncoder.encode("a"),
                "admin@egor.be",
                Role.ADMIN));


    }

}
