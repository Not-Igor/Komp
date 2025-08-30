package com.egor.back_end.service;

import com.egor.back_end.model.FriendRequest;
import com.egor.back_end.model.User;
import com.egor.back_end.repository.FriendRequestRepository;
import com.egor.back_end.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FriendRequestService {
    private final FriendRequestRepository friendRequestRepository;
    private final UserRepository userRepository;

    @Autowired
    public FriendRequestService(FriendRequestRepository friendRequestRepository, UserRepository userRepository) {
        this.friendRequestRepository = friendRequestRepository;
        this.userRepository = userRepository;
    }

    public void sendFriendRequest(Long senderId, String receiverUsername) {
        User sender = userRepository.findById(senderId).orElseThrow(() -> new IllegalArgumentException("Sender not found!"));
        User receiver = userRepository.findByUsername(receiverUsername).orElseThrow(() -> new IllegalArgumentException("Receiver not found!"));

        if (sender.equals(receiver)) {
            throw new IllegalArgumentException("You cannot send a friend request to yourself!");
        }

        if (sender.getFriends().contains(receiver)) {
            throw new IllegalArgumentException("You are already friends with this user!");
        }

        Optional<FriendRequest> existingRequest = friendRequestRepository.findBySenderAndReceiver(sender, receiver);
        if (existingRequest.isPresent() && existingRequest.get().getStatus() == FriendRequest.Status.PENDING) {
            throw new IllegalArgumentException("You already have a pending friend request with this user!");
        }

        // Create and save request.
        FriendRequest friendRequest = new FriendRequest(sender, receiver);
        friendRequestRepository.save(friendRequest);
    }

    public void respondToRequest(Long requestId, boolean accepted) {
        FriendRequest friendRequest = friendRequestRepository.findById(requestId).orElseThrow(() -> new IllegalArgumentException("Friend request not found!"));

        if (friendRequest.getStatus() != FriendRequest.Status.PENDING) {
            throw new IllegalArgumentException("Friend request is not pending!");
        }

        if (accepted) {
            friendRequest.setStatus(FriendRequest.Status.ACCEPTED);

            // Add each other as friends.
            User sender = friendRequest.getSender();
            User receiver = friendRequest.getReceiver();
            sender.getFriends().add(receiver);
            receiver.getFriends().add(sender);
            userRepository.save(sender);
            userRepository.save(receiver);
            friendRequestRepository.save(friendRequest);
        } else {
            friendRequestRepository.delete(friendRequest);
        }
    }

    public List<FriendRequest> getReceivedRequests(Long receiverId) {
        User receiver = userRepository.findById(receiverId).orElseThrow(() -> new IllegalArgumentException("User not found!"));

        return friendRequestRepository.findByReceiverAndStatus(receiver, FriendRequest.Status.PENDING);
    }
}
