package com.egor.back_end.controller;

import com.egor.back_end.dto.user.*;
import com.egor.back_end.model.User;
import com.egor.back_end.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getUsers() {
        return userService.getAllUsers();
    }

    @PostMapping("/login")
    public AuthenticationResponse login(@RequestBody AuthenticationRequest authenticationRequest) {
        return userService.authenticate(authenticationRequest.username(), authenticationRequest.password());
    }

    @PostMapping("/signup")
    public User signup(@Valid @RequestBody UserCreateDto userCreateDto) {
        return userService.signup(userCreateDto);
    }

    @GetMapping("/friends/{userId}")
    public ResponseEntity<List<FriendDto>> getFriends(@PathVariable Long userId) {
        List<FriendDto> friends = userService.getFriendsList(userId);
        return ResponseEntity.ok(friends);
    }

}
