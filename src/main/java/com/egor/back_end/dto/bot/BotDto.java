package com.egor.back_end.dto.bot;

import jakarta.validation.constraints.NotBlank;

public class BotDto {
    private Long id;
    
    @NotBlank
    private String username;

    public BotDto() {}

    public BotDto(Long id, String username) {
        this.id = id;
        this.username = username;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
