package com.egor.back_end.dto.user;

public class UserUpdateDto {
    private String newUsername;
    private String currentPassword;
    private String newPassword;

    public UserUpdateDto() {
    }

    public UserUpdateDto(String newUsername, String currentPassword, String newPassword) {
        this.newUsername = newUsername;
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }

    public String getNewUsername() {
        return newUsername;
    }

    public void setNewUsername(String newUsername) {
        this.newUsername = newUsername;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
