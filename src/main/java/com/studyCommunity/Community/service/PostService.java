package com.studyCommunity.Community.service;

import com.studyCommunity.Community.dto.*;
import com.studyCommunity.Community.entity.Attachment;
import com.studyCommunity.Community.entity.Post;
import com.studyCommunity.Community.exception.BadRequestException;
import com.studyCommunity.Community.exception.ForbiddenException;
import com.studyCommunity.Community.exception.NotFoundException;
import com.studyCommunity.Community.repository.AttachmentRepository;
import com.studyCommunity.Community.repository.CommentRepository;
import com.studyCommunity.Community.repository.PostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final AttachmentRepository attachmentRepository;
    private final AttachmentService attachmentService;
    private final CommentRepository commentRepository;


    @Transactional
    public Long createPost(PostRequest request, String userId) {

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .userId(userId)
                .build();

        postRepository.save(post);

        if(request.getAttachmentIds() == null || request.getAttachmentIds().isEmpty()) {
            return post.getPostId();
        }


        attachmentService.attachToPost(
                request.getAttachmentIds(),
                post,
                userId
        );

        return post.getPostId();
    }


    @Transactional
    public void updatePost(PostUpdateRequest request, String userId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("POST NOT FOUND"));

        if (!post.getUserId().equals(userId)) {
            throw new ForbiddenException("FORBIDDEN");
        }

        post.update(request.getTitle(), request.getContent());
    }


    @Transactional
    public void deletePost(Long postId, String userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("POST NOT FOUND"));

        if(!post.getUserId().equals(userId)) {
            throw new ForbiddenException("FORBIDDEN");
        }


        attachmentService.deleteAllPost(post);
        commentRepository.deleteAllByPost(post);
        postRepository.delete(post);
    }

    @Transactional
    public Page<PostListResponse> getPostList(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return postRepository.findPostList(pageable);
    }



    @Transactional
    public PostDetailResponse getPostDetail(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("POST NOT FOUND"));

        List<AttachmentResponse> attachments = attachmentRepository.findAllByPost(post)
                .stream()
                .map(a -> new AttachmentResponse
                        (a.getAttachmentId(), a.getOriginalFileName(), a.getS3Key()))
                .toList();

        List<CommentResponse> comments = commentRepository.findAllByPostOrderByCreateTimeAsc(post)
                .stream()
                .map(a -> new CommentResponse
                        (a.getCommentId(), a.getContent(), a.getUserId(), a.getCreateTime()))
                .toList();

        return new PostDetailResponse(post.getPostId(),
                post.getTitle(),
                post.getContent(),
                post.getUserId(),
                post.getCreateTime(),
                attachments,
                comments);

    }


    @Transactional
    public void addAttachments(Long postId, List<Long> attachmentIds, String userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("POST NOT FOUND"));

        if (!userId.equals(post.getUserId())) {
            throw new ForbiddenException("게시글 작성자만 첨부파일을 추가할 수 있습니다.");
        }


        if (attachmentIds == null || attachmentIds.isEmpty()) {
            throw new BadRequestException("첨부파일 ID 목록은 비어 있을 수 없습니다.");
        }

        attachmentService.attachToPost(attachmentIds, post, userId);
    }

}
