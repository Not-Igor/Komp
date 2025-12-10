package com.egor.back_end.dto.bot;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class BotCreateDto {
    @NotNull
    @Min(0)
    @Max(3)
    private Integer count;

    @NotNull
    @Size(max = 3)
    private List<String> usernames;

    public BotCreateDto() {}

    public BotCreateDto(Integer count, List<String> usernames) {
        this.count = count;
        this.usernames = usernames;
    }

    // Getters and Setters
    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public List<String> getUsernames() {
        return usernames;
    }

    public void setUsernames(List<String> usernames) {
        this.usernames = usernames;
    }
}
