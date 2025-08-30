package com.egor.back_end.dto.user;

public class RespondFriendRequestDto {
    private Long requestId;
    private boolean accepted;

    public RespondFriendRequestDto() {}

    public RespondFriendRequestDto(Long requestId, boolean accepted) {
        this.requestId = requestId;
        this.accepted = accepted;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }
}
