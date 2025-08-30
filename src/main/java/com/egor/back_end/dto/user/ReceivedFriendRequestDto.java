package com.egor.back_end.dto.user;

public class ReceivedFriendRequestDto {
    private Long requestId;
    private String senderUsername;

    public ReceivedFriendRequestDto() {}

    public ReceivedFriendRequestDto(Long requestId, String senderUsername) {
        this.requestId = requestId;
        this.senderUsername = senderUsername;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }
}
