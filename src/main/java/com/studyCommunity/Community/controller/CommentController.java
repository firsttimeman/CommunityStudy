package com.studyCommunity.Community.controller;

import com.studyCommunity.Community.dto.CommentCreateRequest;
import com.studyCommunity.Community.dto.CommentResponse;
import com.studyCommunity.Community.dto.CommentUpdateRequest;
import com.studyCommunity.Community.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/posts/{postId}")
    public ResponseEntity<BaseResponse<CommentResponse>> createComment(@RequestAttribute("userId") String userId,
                                                         @PathVariable Long postId,
                                                         @Valid @RequestBody CommentCreateRequest request) {
        CommentResponse comment = commentService.createComment(postId, request, userId);
        return ResponseEntity.ok(BaseResponse.of(comment, HttpStatus.OK));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<BaseResponse<Void>> updateComment(@PathVariable Long commentId,
                                                @RequestBody CommentUpdateRequest request,
                                                @Valid @RequestAttribute("userId") String userId) {
        commentService.updateComment(commentId, request, userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(BaseResponse.of(HttpStatus.NO_CONTENT));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<BaseResponse<Void>> deleteComment(
            @PathVariable Long commentId,
            @RequestAttribute("userId") String userId
    ) {
        commentService.delete(commentId, userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(BaseResponse.of(HttpStatus.NO_CONTENT));
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<BaseResponse<List<CommentResponse>>> getComments(
            @PathVariable Long postId
    ) {
        return ResponseEntity.ok(
                BaseResponse.of(commentService.getComments(postId), HttpStatus.OK)
        );
    }


}
