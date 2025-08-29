package com.egor.back_end.dto.user;

import com.egor.back_end.model.Role;

public record AuthenticationResponse(String message, String token, Long id, String username, String email, Role role) {
    public AuthenticationResponse (String message, String token, Long id, String username, String email, Role role) {
        this.message = message;
        this.token = token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
    }
    public String getMessage() {
        return message;
    }
    public String getToken() {
        return token;
    }
    public Long getId() {
        return id;
    }
    public String getUsername() {
        return username;
    }
    public String getEmail() {
        return email;
    }
    public Role getRole() {
        return role;
    }
}
