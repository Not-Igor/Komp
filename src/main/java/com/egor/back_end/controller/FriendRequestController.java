package com.egor.back_end.controller;

import com.egor.back_end.dto.user.ReceivedFriendRequestDto;
import com.egor.back_end.dto.user.RespondFriendRequestDto;
import com.egor.back_end.dto.user.SendFriendRequestDto;
import com.egor.back_end.model.FriendRequest;
import com.egor.back_end.service.FriendRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/friends")
public class FriendRequestController {
    private final FriendRequestService friendRequestService;

    @Autowired
    public FriendRequestController(FriendRequestService friendRequestService) {
        this.friendRequestService = friendRequestService;
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendFriendRequest(@RequestBody SendFriendRequestDto dto) {
        friendRequestService.sendFriendRequest(dto.getSenderId(), dto.getReceiverUsername());
        return ResponseEntity.ok("Friend request sent");
    }

    @PostMapping("/respond")
    public ResponseEntity<?> respondToRequest(@RequestBody RespondFriendRequestDto dto) {
        friendRequestService.respondToRequest(dto.getRequestId(), dto.isAccepted());
        return ResponseEntity.ok("Friend request response processed");
    }

    @GetMapping("/received/{userId}")
    public ResponseEntity<List<ReceivedFriendRequestDto>> getReceivedRequests(@PathVariable Long userId) {
        List<FriendRequest> requests = friendRequestService.getReceivedRequests(userId);
        List<ReceivedFriendRequestDto> dtoList = requests.stream()
                .map(req -> new ReceivedFriendRequestDto(req.getId(), req.getSender().getUsername()))
                .toList();
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/sent/{userId}")
    public ResponseEntity<List<ReceivedFriendRequestDto>> getSentRequests(@PathVariable Long userId) {
        List<FriendRequest> requests = friendRequestService.getSentRequests(userId);
        List<ReceivedFriendRequestDto> dtoList = requests.stream()
                .map(req -> new ReceivedFriendRequestDto(req.getId(), req.getReceiver().getUsername()))
                .toList();
        return ResponseEntity.ok(dtoList);
    }

    @DeleteMapping("/cancel/{requestId}/{userId}")
    public ResponseEntity<?> cancelFriendRequest(@PathVariable Long requestId, @PathVariable Long userId) {
        try {
            friendRequestService.cancelFriendRequest(requestId, userId);
            return ResponseEntity.ok("Friend request cancelled");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
