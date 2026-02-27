package com.studyCommunity.Community.service;

import com.studyCommunity.Community.dto.CommentCreateRequest;
import com.studyCommunity.Community.dto.CommentResponse;
import com.studyCommunity.Community.dto.CommentUpdateRequest;
import com.studyCommunity.Community.entity.Comment;
import com.studyCommunity.Community.entity.Post;
import com.studyCommunity.Community.exception.ForbiddenException;
import com.studyCommunity.Community.exception.NotFoundException;
import com.studyCommunity.Community.repository.CommentRepository;
import com.studyCommunity.Community.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    @Transactional
    public CommentResponse createComment(Long postId, CommentCreateRequest request, String userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("POST NOT FOUND"));

        Comment comment = Comment.createComment(request.getContent(), userId, post);
        commentRepository.save(comment);

        postRepository.incrementCommentCount(postId);

        return new CommentResponse(comment.getCommentId(),
                comment.getContent(),
                comment.getUserId(),
                comment.getCreateTime());
    }

    @Transactional
    public void updateComment(Long commentId, CommentUpdateRequest request, String userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("COMMENT NOT FOUND"));


        comment.updateComment(request.getContent(), userId);
    }

    @Transactional
    public void delete(Long commentId, String userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("COMMENT NOT FOUND"));

        if (!comment.getUserId().equals(userId)) {
            throw new ForbiddenException("댓글 작성자만 삭제할 수 있습니다.");
        }

        Long postId = comment.getPost().getPostId();

        commentRepository.delete(comment);
        postRepository.decrementCommentCount(postId);
    }


    @Transactional
    public List<CommentResponse>getComments(Long postId, int page, int size)  {
        if (!postRepository.existsById(postId)) {
            throw new NotFoundException("POST NOT FOUND");
        }

        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createTime"));

        return commentRepository.findByPost_PostIdOrderByCreateTimeAsc(postId, pageable)
                .stream()
                .map(c -> new CommentResponse(
                        c.getCommentId(),
                        c.getContent(),
                        c.getUserId(),
                        c.getCreateTime()
                ))
                .toList();
    }
}
