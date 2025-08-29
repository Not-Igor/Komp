package com.egor.back_end.dto.user;

public record AuthenticationRequest(
        String username,
        String password
    ){
}
