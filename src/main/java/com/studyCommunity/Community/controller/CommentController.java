package com.studyCommunity.Community.controller;

import com.studyCommunity.Community.dto.CommentCreateRequest;
import com.studyCommunity.Community.dto.CommentResponse;
import com.studyCommunity.Community.dto.CommentUpdateRequest;
import com.studyCommunity.Community.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/{postId}/comments")
    public ResponseEntity<CommentResponse> createComment(@PathVariable Long postId,
                                          @RequestBody CommentCreateRequest request,
                                          @RequestAttribute("userId") String userId) {
        CommentResponse comment = commentService.createComment(postId, request, userId);
        return ResponseEntity.ok(comment);
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<Object> updateComment(@PathVariable Long commentId,
                                                @RequestBody CommentUpdateRequest request,
                                                @RequestAttribute("userId") String userId) {
        commentService.updateComment(commentId, request, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @RequestHeader("X-User-Id") String userId
    ) {
        commentService.delete(commentId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(
            @PathVariable Long postId
    ) {
        return ResponseEntity.ok(commentService.getComments(postId));
    }


}
