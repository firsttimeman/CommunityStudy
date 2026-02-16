package com.studyCommunity.Community.controller;

import com.studyCommunity.Community.dto.*;
import com.studyCommunity.Community.exception.BadRequestException;
import com.studyCommunity.Community.service.CommentService;
import com.studyCommunity.Community.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
@Profile("loadTest")
public class TestController {

    private final CommentService commentService;
    private final PostService postService;

    private static final long HOT_POST_ID = 1L; // ✅ 여기를 고정

    @PostMapping("/hot/comment")
    public ResponseEntity<BaseResponse<CommentResponse>> hotComment(
            @RequestAttribute("userId") String userId
    ) {
        // content 길이는 너 validation에 맞게
        CommentCreateRequest req = new CommentCreateRequest("load-test-" + userId + "-" + System.nanoTime());
        CommentResponse res = commentService.createComment(HOT_POST_ID, req, userId);
        return ResponseEntity.ok(BaseResponse.of(res, HttpStatus.OK));
    }

    @GetMapping("/hot/detail")
    public ResponseEntity<BaseResponse<PostDetailResponse>> hotDetail() {
        // 상세 조회가 무겁게(join/fetch) 되어있을수록 그래프가 잘 보임
        return ResponseEntity.ok(
                BaseResponse.of(postService.getPostDetail(HOT_POST_ID), HttpStatus.OK)
        );
    }

}
