package com.egor.back_end.service;

import com.egor.back_end.dto.user.AuthenticationResponse;
import com.egor.back_end.dto.user.FriendDto;
import com.egor.back_end.dto.user.UserCreateDto;
import com.egor.back_end.dto.user.UserDto;
import com.egor.back_end.dto.user.UserProfileDto;
import com.egor.back_end.dto.user.UserUpdateDto;
import com.egor.back_end.exceptions.SignupException;
import com.egor.back_end.model.Role;
import com.egor.back_end.model.User;
import com.egor.back_end.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public AuthenticationResponse authenticate(String username, String password) {
        final var usernamePasswordAuthentication = new UsernamePasswordAuthenticationToken(username, password);
        final var authentication = authenticationManager.authenticate(usernamePasswordAuthentication);
        final var user = ((UserDetailsImpl) authentication.getPrincipal()).getUser();
        final var token = jwtService.generateToken(user);
        return new AuthenticationResponse(
                "Authentication successful.",
                token,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
    }

    public User signup(UserCreateDto userCreateDto) {
        if (userRepository.existsByUsername(userCreateDto.username())) {
            throw new SignupException("Username is already in use!");
        }

        User user = new User(
                userCreateDto.username(),
                passwordEncoder.encode(userCreateDto.password()),
                Role.USER
        );
        return userRepository.save(user);
    }

    public List<FriendDto> getFriendsList(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return user.getFriends().stream()
                .map(friend -> new FriendDto(friend.getId(), friend.getUsername()))
                .collect(Collectors.toList());
    }

    public UserProfileDto getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        String avatarUrl = generateAvatarUrl(user.getUsername());
        
        return new UserProfileDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                avatarUrl
        );
    }

    private String generateAvatarUrl(String username) {
        return "https://api.dicebear.com/7.x/avataaars/svg?seed=" + username;
    }

    public List<UserDto> searchUserByUsername(String username) {
        List<User> users = userRepository.findByUsernameContainingIgnoreCase(username);
        
        if (users.isEmpty()) {
            throw new IllegalArgumentException("No users found");
        }
        
        return users.stream()
                .map(user -> new UserDto(user.getId(), user.getUsername(), user.getEmail(), user.getRole()))
                .limit(5) // Limit to 5 results
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateUserProfile(UserUpdateDto dto, String currentUsername) {
        log.info("Attempting to update profile for user: {}", currentUsername);
        log.info("Update DTO: newUsername='{}', newPassword is present: {}", dto.getNewUsername(), dto.getNewPassword() != null && !dto.getNewPassword().isBlank());

        try {
            User user = userRepository.findByUsername(currentUsername)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            // Update username
            if (dto.getNewUsername() != null && !dto.getNewUsername().isBlank() && !dto.getNewUsername().equals(currentUsername)) {
                log.info("Updating username for user: {}", currentUsername);
                if (userRepository.existsByUsername(dto.getNewUsername())) {
                    log.warn("Username '{}' is already taken.", dto.getNewUsername());
                    throw new IllegalArgumentException("Username is already taken");
                }
                user.setUsername(dto.getNewUsername());
                log.info("Username updated to: {}", dto.getNewUsername());
            }

            // Update password
            if (dto.getNewPassword() != null && !dto.getNewPassword().isBlank()) {
                log.info("Updating password for user: {}", currentUsername);
                if (dto.getCurrentPassword() == null || !passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
                    log.warn("Invalid current password attempt for user: {}", currentUsername);
                    throw new IllegalArgumentException("Invalid current password");
                }
                user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
                log.info("Password updated for user: {}", currentUsername);
            }

            log.info("Saving updated user: {}", user.getUsername());
            userRepository.save(user);
            log.info("Successfully saved user: {}", user.getUsername());
        } catch (Exception e) {
            log.error("Unhandled exception during profile update for user: " + currentUsername, e);
            throw e; // Re-throw to allow global exception handler to catch it
        }
    }
}
