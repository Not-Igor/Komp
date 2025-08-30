package com.egor.back_end.dto.user;

public class SendFriendRequestDto {
    private Long senderId;
    private String receiverUsername;

    public SendFriendRequestDto() {}

    public SendFriendRequestDto(Long senderId, String receiverUsername) {
        this.senderId = senderId;
        this.receiverUsername = receiverUsername;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getReceiverUsername() {
        return receiverUsername;
    }

    public void setReceiverUsername(String receiverUsername) {
        this.receiverUsername = receiverUsername;
    }
}
