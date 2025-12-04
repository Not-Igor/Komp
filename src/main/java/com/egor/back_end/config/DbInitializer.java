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

        // Admin user
        final var admin = userRepository.save(new User(
                "admin",
                passwordEncoder.encode("admin123"),
                "admin@kompapp.com",
                Role.ADMIN));

        // Create 20 test users with password "admin123"
        userRepository.save(new User("alice_wonder", passwordEncoder.encode("admin123"), "alice@example.com", Role.USER));
        userRepository.save(new User("bob_builder", passwordEncoder.encode("admin123"), "bob@example.com", Role.USER));
        userRepository.save(new User("charlie_brown", passwordEncoder.encode("admin123"), "charlie@example.com", Role.USER));
        userRepository.save(new User("diana_prince", passwordEncoder.encode("admin123"), "diana@example.com", Role.USER));
        userRepository.save(new User("ethan_hunt", passwordEncoder.encode("admin123"), "ethan@example.com", Role.USER));
        userRepository.save(new User("fiona_apple", passwordEncoder.encode("admin123"), "fiona@example.com", Role.USER));
        userRepository.save(new User("george_west", passwordEncoder.encode("admin123"), "george@example.com", Role.USER));
        userRepository.save(new User("hannah_montana", passwordEncoder.encode("admin123"), "hannah@example.com", Role.USER));
        userRepository.save(new User("ivan_terrible", passwordEncoder.encode("admin123"), "ivan@example.com", Role.USER));
        userRepository.save(new User("julia_roberts", passwordEncoder.encode("admin123"), "julia@example.com", Role.USER));
        userRepository.save(new User("kevin_hart", passwordEncoder.encode("admin123"), "kevin@example.com", Role.USER));
        userRepository.save(new User("laura_croft", passwordEncoder.encode("admin123"), "laura@example.com", Role.USER));
        userRepository.save(new User("mike_tyson", passwordEncoder.encode("admin123"), "mike@example.com", Role.USER));
        userRepository.save(new User("nina_simone", passwordEncoder.encode("admin123"), "nina@example.com", Role.USER));
        userRepository.save(new User("oliver_twist", passwordEncoder.encode("admin123"), "oliver@example.com", Role.USER));
        userRepository.save(new User("petra_stone", passwordEncoder.encode("admin123"), "petra@example.com", Role.USER));
        userRepository.save(new User("quinn_fabray", passwordEncoder.encode("admin123"), "quinn@example.com", Role.USER));
        userRepository.save(new User("rachel_green", passwordEncoder.encode("admin123"), "rachel@example.com", Role.USER));
        userRepository.save(new User("steve_jobs", passwordEncoder.encode("admin123"), "steve@example.com", Role.USER));
        userRepository.save(new User("tina_turner", passwordEncoder.encode("admin123"), "tina@example.com", Role.USER));
    }

}
