package com.egor.back_end.service;

import com.egor.back_end.dto.user.*;
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
        final var usernamePasswordAuthentication = new UsernamePasswordAuthenticationToken(username.toLowerCase(), password);
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
        String lowercaseUsername = userCreateDto.username().toLowerCase();
        if (userRepository.existsByUsername(lowercaseUsername)) {
            throw new SignupException("Username is already in use!");
        }

        User user = new User(
                lowercaseUsername,
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
        return "https://api.dicebear.com/7.x/pixel-art/svg?seed=" + username;
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
    public AuthenticationResponse updateUserProfile(UserUpdateDto dto, String currentUsername) {
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + currentUsername));

        boolean usernameChanged = false;
        // Update username
        if (dto.getNewUsername() != null && !dto.getNewUsername().isBlank() && !dto.getNewUsername().equals(currentUsername)) {
            String lowercaseNewUsername = dto.getNewUsername().toLowerCase();
            if (userRepository.existsByUsername(lowercaseNewUsername)) {
                throw new IllegalArgumentException("Username is already taken");
            }
            user.setUsername(lowercaseNewUsername);
            usernameChanged = true;
        }

        // Update password
        if (dto.getNewPassword() != null && !dto.getNewPassword().isBlank()) {
            if (dto.getCurrentPassword() == null || !passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
                throw new IllegalArgumentException("Invalid current password");
            }
            user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        }

        User savedUser = userRepository.save(user);

        if (usernameChanged) {
            final var token = jwtService.generateToken(savedUser);
            return new AuthenticationResponse(
                    "Profile updated. New token issued.",
                    token,
                    savedUser.getId(),
                    savedUser.getUsername(),
                    savedUser.getEmail(),
                    savedUser.getRole()
            );
        }

        return new AuthenticationResponse("Profile updated successfully.", null, savedUser.getId(), savedUser.getUsername(), savedUser.getEmail(), savedUser.getRole());
    }
}
