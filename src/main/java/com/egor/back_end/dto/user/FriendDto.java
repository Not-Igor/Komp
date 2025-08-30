package com.egor.back_end.dto.user;

public class FriendDto {
    private Long id;
    private String username;

    public FriendDto() {}

    public FriendDto(Long id, String username) {
        this.id = id;
        this.username = username;
    }

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

    @Override
    public String toString() {
        return "FriendDto{" +
                "id=" + id +
                ", username='" + username + '\'' +
                '}';
    }
}
