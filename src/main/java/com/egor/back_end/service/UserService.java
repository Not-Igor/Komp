package com.egor.back_end.service;

import com.egor.back_end.dto.user.AuthenticationResponse;
import com.egor.back_end.dto.user.FriendDto;
import com.egor.back_end.dto.user.UserCreateDto;
import com.egor.back_end.dto.user.UserDto;
import com.egor.back_end.dto.user.UserProfileDto;
import com.egor.back_end.exceptions.SignupException;
import com.egor.back_end.model.Role;
import com.egor.back_end.model.User;
import com.egor.back_end.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
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

}
