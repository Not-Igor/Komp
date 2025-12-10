package com.egor.back_end.service;

import com.egor.back_end.model.FriendRequest;
import com.egor.back_end.model.NotificationType;
import com.egor.back_end.model.User;
import com.egor.back_end.repository.FriendRequestRepository;
import com.egor.back_end.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class FriendRequestService {
    private final FriendRequestRepository friendRequestRepository;
    private final UserRepository userRepository;

    private final NotificationService notificationService;

    @Autowired
    public FriendRequestService(FriendRequestRepository friendRequestRepository, 
                               UserRepository userRepository,
                               NotificationService notificationService) {
        this.friendRequestRepository = friendRequestRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
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
        
        // Create notification for receiver
        notificationService.createNotification(
            receiver,
            NotificationType.FRIEND_REQUEST,
            sender.getUsername() + " sent you a friend request",
            friendRequest.getId()
        );
    }

    public void respondToRequest(Long requestId, boolean accepted) {
        FriendRequest friendRequest = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Friend request not found!"));

        if (friendRequest.getStatus() != FriendRequest.Status.PENDING) {
            throw new IllegalArgumentException("Friend request is not pending!");
        }

        if (accepted) {
            friendRequest.setStatus(FriendRequest.Status.ACCEPTED);
            friendRequestRepository.save(friendRequest);

            // Add each other as friends
            User sender = friendRequest.getSender();
            User receiver = friendRequest.getReceiver();
            
            // Add bidirectional friendship
            sender.getFriends().add(receiver);
            receiver.getFriends().add(sender);
            
            // Save both users
            userRepository.save(sender);
            userRepository.save(receiver);
            
            // Notify sender that request was accepted
            notificationService.createNotification(
                sender,
                NotificationType.FRIEND_REQUEST_ACCEPTED,
                receiver.getUsername() + " accepted your friend request",
                null
            );
        } else {
            // Delete the friend request if rejected
            friendRequestRepository.delete(friendRequest);
        }
    }

    public List<FriendRequest> getReceivedRequests(Long receiverId) {
        User receiver = userRepository.findById(receiverId).orElseThrow(() -> new IllegalArgumentException("User not found!"));

        return friendRequestRepository.findByReceiverAndStatus(receiver, FriendRequest.Status.PENDING);
    }

    public List<FriendRequest> getSentRequests(Long senderId) {
        User sender = userRepository.findById(senderId).orElseThrow(() -> new IllegalArgumentException("User not found!"));

        return friendRequestRepository.findBySenderAndStatus(sender, FriendRequest.Status.PENDING);
    }

    public void cancelFriendRequest(Long requestId, Long userId) {
        FriendRequest friendRequest = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Friend request not found!"));

        if (!friendRequest.getSender().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only cancel your own friend requests!");
        }

        if (friendRequest.getStatus() != FriendRequest.Status.PENDING) {
            throw new IllegalArgumentException("Friend request is not pending!");
        }

        friendRequestRepository.delete(friendRequest);
    }

    public List<User> getFriendSuggestions(Long userId, int limit) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found!"));
        
        Set<User> currentFriends = user.getFriends();
        Set<User> suggestions = new HashSet<>();
        
        // Get friends of friends
        for (User friend : currentFriends) {
            Set<User> friendsOfFriend = friend.getFriends();
            for (User potentialFriend : friendsOfFriend) {
                // Don't suggest:
                // 1. The user themselves
                // 2. Current friends
                // 3. Users with pending/existing friend requests
                if (!potentialFriend.equals(user) 
                    && !currentFriends.contains(potentialFriend)
                    && !friendRequestRepository.existsRequestBetweenUsers(user, potentialFriend)) {
                    suggestions.add(potentialFriend);
                }
            }
        }
        
        // Sort by number of mutual friends (descending)
        return suggestions.stream()
                .sorted((u1, u2) -> {
                    int mutualCount1 = getMutualFriendsCount(user, u1);
                    int mutualCount2 = getMutualFriendsCount(user, u2);
                    return Integer.compare(mutualCount2, mutualCount1);
                })
                .limit(limit)
                .toList();
    }
    
    public int getMutualFriendsCount(User user1, User user2) {
        Set<User> friends1 = user1.getFriends();
        Set<User> friends2 = user2.getFriends();
        
        Set<User> mutualFriends = new HashSet<>(friends1);
        mutualFriends.retainAll(friends2);
        
        return mutualFriends.size();
    }
    
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found!"));
    }
}
