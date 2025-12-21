package com.studyCommunity.Community.service;

import com.studyCommunity.Community.dto.PostDetailResponse;
import com.studyCommunity.Community.dto.PostListResponse;
import com.studyCommunity.Community.dto.PostRequest;
import com.studyCommunity.Community.dto.PostUpdateRequest;
import com.studyCommunity.Community.entity.Attachment;
import com.studyCommunity.Community.entity.Comment;
import com.studyCommunity.Community.entity.Post;
import com.studyCommunity.Community.repository.AttachmentRepository;
import com.studyCommunity.Community.repository.CommentRepository;
import com.studyCommunity.Community.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    PostRepository postRepository;

    @Mock
    AttachmentRepository attachmentRepository;

    @Mock AttachmentService attachmentService;

    @Mock
    CommentRepository commentRepository;

    @InjectMocks
    PostService postService;

    @Test
    void createPost() {
        PostRequest request = new PostRequest("t", "c", null);
        String userId = "user-1";

        when(postRepository.save(any(Post.class))).thenAnswer(inv -> {
            Post p = inv.getArgument(0);
            return p;
        });

        // when
        Long result = postService.createPost(request, userId);

        // then
        verify(postRepository).save(any(Post.class));
        verifyNoInteractions(attachmentRepository);
        verifyNoInteractions(attachmentService);
        verifyNoInteractions(commentRepository);
    }

    @Test
    void updatePost() {

        Long postId = 1L;
        PostUpdateRequest request = new PostUpdateRequest("t", "c");
        String userId = "user-1";

        Post post = Post.builder()
                .title("ot")
                .content("oc")
                .userId("user-1")
                .build();

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        postService.updatePost(request, userId, postId);

        assertThat(post.getTitle()).isEqualTo("t");
        assertThat(post.getContent()).isEqualTo("c");
        verify(postRepository).findById(postId);
    }

    @Test
    void deletePost() {
        Long postId = 1L;
        String userId = "user-1";

        Post post = Post.builder()
                .title("ot")
                .content("oc")
                .userId("user-1")
                .build();

        when(postRepository.findById(postId))
                .thenReturn(Optional.of(post));

        postService.deletePost(postId, userId);

        InOrder inOrder = inOrder(    attachmentService,
                commentRepository,
                postRepository);

        inOrder.verify(attachmentService).deleteAllPost(post);
        inOrder.verify(commentRepository).deleteAllByPost(post);
        inOrder.verify(postRepository).delete(post);
    }

    @Test //todo 다시 이해하기
    void getPostList() {
        // given
        int page = 0;
        int size = 20;

        // repository가 돌려준다고 "가정"할 Page
        PostListResponse r1 = new PostListResponse(1L, "t1", "u1", null, 2L, 1L);
        PostListResponse r2 = new PostListResponse(2L, "t2", "u2", null, 0L, 0L);

        Page<PostListResponse> repoResult =
                new PageImpl<>(List.of(r1, r2), PageRequest.of(page, size), 2);

        when(postRepository.findPostList(any(Pageable.class))).thenReturn(repoResult);

        // when
        Page<PostListResponse> result = postService.getPostList(page, size);

        // then: 결과 검증
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);

        // then: pageable이 제대로 전달됐는지 검증
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(postRepository).findPostList(captor.capture());

        Pageable captured = captor.getValue();
        assertThat(captured.getPageNumber()).isEqualTo(page);
        assertThat(captured.getPageSize()).isEqualTo(size);

        // 나머지 repo는 건드리지 않음
        verifyNoInteractions(attachmentRepository, attachmentService, commentRepository);
    }

    @Test//todo 다시 이해하기
    void getPostDetail() {
        // given
        Long postId = 1L;

        Post post = mock(Post.class);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        when(post.getPostId()).thenReturn(postId);
        when(post.getTitle()).thenReturn("title");
        when(post.getContent()).thenReturn("content");
        when(post.getUserId()).thenReturn("user-1");
        when(post.getCreateTime()).thenReturn(
                LocalDateTime.of(2025, 12, 20, 12, 0)
        );

        // attachment mock
        Attachment attachment = mock(Attachment.class);
        when(attachment.getAttachmentId()).thenReturn(10L);
        when(attachment.getOriginalFileName()).thenReturn("file.png");
        when(attachment.getS3Key()).thenReturn("posts/uuid.png");

        when(attachmentRepository.findAllByPost(post))
                .thenReturn(List.of(attachment));

        // comment mock
        Comment comment = mock(Comment.class);
        when(comment.getCommentId()).thenReturn(100L);
        when(comment.getContent()).thenReturn("hello");
        when(comment.getUserId()).thenReturn("user-2");
        when(comment.getCreateTime()).thenReturn(
                LocalDateTime.of(2025, 12, 20, 11, 50)
        );

        when(commentRepository.findAllByPostOrderByCreateTimeAsc(post))
                .thenReturn(List.of(comment));

        // when
        PostDetailResponse result = postService.getPostDetail(postId);

        assertThat(result.getPostId()).isEqualTo(postId);
        assertThat(result.getTitle()).isEqualTo("title");
        assertThat(result.getContent()).isEqualTo("content");
        assertThat(result.getUserId()).isEqualTo("user-1");

        assertThat(result.getAttachments()).hasSize(1);
        assertThat(result.getAttachments().get(0).getOriginalFileName())
                .isEqualTo("file.png");

        assertThat(result.getComments()).hasSize(1);
        assertThat(result.getComments().get(0).getContent())
                .isEqualTo("hello");

        // then (호출 검증)
        verify(postRepository).findById(postId);
        verify(attachmentRepository).findAllByPost(post);
        verify(commentRepository).findAllByPostOrderByCreateTimeAsc(post);
    }
}