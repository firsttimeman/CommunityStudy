package com.studyCommunity.Community.controller;

import com.studyCommunity.Community.dto.*;
import com.studyCommunity.Community.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    /**
     * 게시글 생성
     */
    @PostMapping
    public ResponseEntity<PostCreateResponse> createPost(
            @RequestAttribute("userId") String userId,
//            @RequestHeader("X-User-Id") String userId,
            @RequestBody PostRequest request
    ) {
        Long postId = postService.createPost(request, userId);
        return ResponseEntity.ok(new PostCreateResponse(postId));
    }

    /**
     * 게시글 수정
     */
    @PutMapping("/{postId}")
    public void updatePost(
            @RequestAttribute("userId") String userId,
            @PathVariable Long postId,
            @RequestBody PostUpdateRequest request
    ) {
        postService.updatePost(request, userId, postId);
    }

    /**
     * 게시글 삭제
     */
    @DeleteMapping("/{postId}")
    public void deletePost(
            @RequestAttribute("userId") String userId,
            @PathVariable Long postId
    ) {
        postService.deletePost(postId, userId);
    }

    /**
     * 게시글 목록 조회 (페이징)
     */
    @GetMapping
    public Page<PostListResponse> getPostList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return postService.getPostList(page, size);
    }

    /**
     * 게시글 상세 조회
     */
    @GetMapping("/{postId}")
    public PostDetailResponse getPostDetail(
            @PathVariable Long postId
    ) {
        return postService.getPostDetail(postId);
    }

}
