package com.egor.back_end.dto.user;

public class AvatarUrlDto {
    private String avatarUrl;

    public AvatarUrlDto() {
    }

    public AvatarUrlDto(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
