package com.egor.back_end.repository;

import com.egor.back_end.model.FriendRequest;
import com.egor.back_end.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    // Find all requests sent to a specific user with a specific status
    List<FriendRequest> findByReceiverAndStatus(User receiver, FriendRequest.Status status);
    // Find a specific request between sender and receiver
    Optional<FriendRequest> findBySenderAndReceiver(User sender, User receiver);
    // List of requests that a user has sent
    List<FriendRequest> findBySender(User sender);
    // List of pending requests that a user has sent
    List<FriendRequest> findBySenderAndStatus(User sender, FriendRequest.Status status);
}
