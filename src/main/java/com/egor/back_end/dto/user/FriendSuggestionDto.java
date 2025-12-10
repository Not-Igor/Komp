package com.egor.back_end.dto.user;

public class FriendSuggestionDto {
    private Long id;
    private String username;
    private int mutualFriendsCount;

    public FriendSuggestionDto() {}

    public FriendSuggestionDto(Long id, String username, int mutualFriendsCount) {
        this.id = id;
        this.username = username;
        this.mutualFriendsCount = mutualFriendsCount;
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

    public int getMutualFriendsCount() {
        return mutualFriendsCount;
    }

    public void setMutualFriendsCount(int mutualFriendsCount) {
        this.mutualFriendsCount = mutualFriendsCount;
    }
}
