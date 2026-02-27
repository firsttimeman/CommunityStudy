package com.studyCommunity.Community.controller;

import com.studyCommunity.Community.dto.*;
import com.studyCommunity.Community.exception.BadRequestException;
import com.studyCommunity.Community.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    /**
     * 게시글 생성
     */
    @PostMapping
    public ResponseEntity<BaseResponse<PostCreateResponse>> createPost(
            @RequestAttribute("userId") String userId,
            @Valid @RequestBody PostRequest request
    ) {
        Long postId = postService.createPost(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.of(new PostCreateResponse(postId), HttpStatus.CREATED));
    }

    /**
     * 게시글 수정
     */
    @PutMapping("/{postId}")
    public ResponseEntity<BaseResponse<Void>> updatePost(
            @RequestAttribute("userId") String userId,
            @PathVariable Long postId,
            @Valid @RequestBody PostUpdateRequest request
    ) {
        postService.updatePost(request, userId, postId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(BaseResponse.of(HttpStatus.NO_CONTENT));
    }

    /**
     * 게시글 삭제
     *
     * @return
     */
    @DeleteMapping("/{postId}")
    public ResponseEntity<BaseResponse<Void>> deletePost(
            @RequestAttribute("userId") String userId,
            @PathVariable Long postId
    ) {
        postService.deletePost(postId, userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(BaseResponse.of(HttpStatus.NO_CONTENT));
    }

    /**
     * 게시글 목록 조회 (페이징)
     */
    @GetMapping
    public ResponseEntity<BaseResponse<Page<PostListResponse>>> getPostList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (page < 0) page = 0;
//        if (page < 0) throw new BadRequestException("page는 0 이상이어야 합니다.");
        //if (size <= 0 || size > 100) throw new BadRequestException("size는 1 ~ 100 사이어야 합니다.");

        if (size < 1) size = 1;
        if (size > 100) size = 100;

        return ResponseEntity.ok(
                BaseResponse.of(postService.getPostList(page, size), HttpStatus.OK)
        );
    }

    @GetMapping("/popular")
    public ResponseEntity<BaseResponse<Page<PostListResponse>>> getPopularFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) throws InterruptedException {
        if (page < 0) page = 0;
        if (size < 1) size = 1;
        if (size > 100) size = 100;

        return ResponseEntity.ok(
                BaseResponse.of(postService.getPopularFeed(page, size), HttpStatus.OK)
        );
    }





    /**
     * 게시글 상세 조회
     */
    @GetMapping("/{postId}")
    public ResponseEntity<BaseResponse<PostDetailResponse>> getPostDetail(
            @PathVariable Long postId
    ) {
        return ResponseEntity.ok(
                BaseResponse.of(postService.getPostDetail(postId), HttpStatus.OK)
        );
    }

    @PostMapping("/{postId}/attachments")
    public ResponseEntity<BaseResponse<Void>> addAttachments(
            @RequestAttribute("userId") String userId,
            @PathVariable Long postId,
            @RequestBody List<Long> attachmentIds
    ) {
        postService.addAttachments(postId, attachmentIds, userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(BaseResponse.of(HttpStatus.NO_CONTENT));
    }




}
