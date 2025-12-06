package com.egor.back_end.dto.user;

public class AvatarUpdateDto {
    private String avatarUrl;

    public AvatarUpdateDto() {
    }

    public AvatarUpdateDto(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
