package com.studyCommunity.Community.service;

import com.studyCommunity.Community.dto.CommentCreateRequest;
import com.studyCommunity.Community.dto.CommentResponse;
import com.studyCommunity.Community.redis.DistributeLock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CommentFacade {
    private final CommentService commentService;

    @DistributeLock(
            key = "'post:' + #postId + ':commentCount'",
            waitTime = 150,
            leaseTime = 3000,
            timeUnit = TimeUnit.MILLISECONDS
    )
    public CommentResponse createCommentWithLock(Long postId, CommentCreateRequest request, String userId) {
        return commentService.createComment(postId, request, userId);
    }
}
