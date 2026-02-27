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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;


@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private CommentService commentService;

    @Test
    void 댓글을_정상적으로_생성한다_verify() {
        Long postId = 1L;
        String userId = "userId";
        CommentCreateRequest req = new CommentCreateRequest("content");

        Post post = mock(Post.class);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(commentRepository.save(any(Comment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CommentResponse res = commentService.createComment(postId, req, userId);

        assertNotNull(res);
        assertEquals("content", res.getContent());
        assertEquals(userId, res.getUserId());

        verify(postRepository).findById(postId);

        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(captor.capture());

        Comment saved = captor.getValue();
        assertEquals("content", saved.getContent());
        assertEquals(userId, saved.getUserId());
        assertSame(post, saved.getPost());

    }


    @Test
    void 댓글을_정상적으로_작성한다_간단() {

        Long postId = 1L;
        String userId = "userId";
        CommentCreateRequest req = new CommentCreateRequest("content");

        Post post = mock(Post.class);

        when(postRepository.findById(postId))
                .thenReturn(Optional.of(post));

        when(commentRepository.save(any(Comment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CommentResponse res = commentService.createComment(postId, req, userId);

        assertNotNull(res);
        assertEquals("content", res.getContent());
        assertEquals(userId, res.getUserId());
    }




    @Test
    void 포스트를_찾을_수_없는_경우_실패한다() {

        Long postId = 1L;
        String userId = "userId";
        CommentCreateRequest req = new CommentCreateRequest("content");

        when(postRepository.findById(postId))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> commentService.createComment(postId, req, userId)
        );

        verify(commentRepository, never()).save(any());
    }

    @Test
    void 코멘트_업데이트_성공 () {
        /**
         * 가짜의 코멘트를 만들어서 테스트를 한다.
         */


        Long commentId = 1L;
        String userId = "userId";
        CommentUpdateRequest req = new CommentUpdateRequest("new content");

        Post post = mock(Post.class);

        Comment comment = Comment.createComment("old content", userId, post);

        when(commentRepository.findById(commentId))
                .thenReturn(Optional.of(comment));

        commentService.updateComment(commentId, req, userId);

        assertEquals("new content", comment.getContent());
    }

    @Test
    void 코멘트_삭제_성공() {
        /**
         * 가짜의 코멘트를 미리 만들고 삭제가 되었는지 사이즈를 검증??
         */

        Long commentId = 1L;
        String userId = "userId";

        Post post = mock(Post.class);

        Comment comment = Comment.createComment("content", userId, post);

        when(commentRepository.findById(commentId))
                .thenReturn(Optional.of(comment));

        commentService.delete(commentId, userId);

        verify(commentRepository).delete(comment);
    }

    @Test
    void 코멘트_삭제_검증_댓글이_존재_X() {

        Long commentId = 1L;
        String userId = "userId";

        when(commentRepository.findById(commentId))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> commentService.delete(commentId, userId)
        );

        verify(commentRepository, never()).delete(any());
    }

    @Test
    void  코멘트_삭제_검증_작성자_동일_X() {

        Long commentId = 1L;
        Post post = mock(Post.class);

        Comment comment = Comment.createComment("content", "owner", post);

        when(commentRepository.findById(commentId))
                .thenReturn(Optional.of(comment));

        assertThrows(
            ForbiddenException.class,
                () -> commentService.delete(commentId, "other")
        );

        verify(commentRepository, never()).delete(any());
    }

    @Test
    void 코멘트_가지고오기_성공() {

        Long postId = 1L;

        // ✅ 이거 추가
        when(postRepository.existsById(postId)).thenReturn(true);

        Comment c1 = mock(Comment.class);
        Comment c2 = mock(Comment.class);

        when(c1.getCommentId()).thenReturn(1L);
        when(c1.getContent()).thenReturn("c1 content");
        when(c1.getUserId()).thenReturn("u1");
        when(c1.getCreateTime()).thenReturn(null);

        when(c2.getCommentId()).thenReturn(2L);
        when(c2.getContent()).thenReturn("c2 content");
        when(c2.getUserId()).thenReturn("u2");
        when(c2.getCreateTime()).thenReturn(null);

        when(commentRepository.findByPost_PostIdOrderByCreateTimeAsc(eq(postId), any(Pageable.class)))
                .thenReturn(List.of(c1, c2));

        List<CommentResponse> res = commentService.getComments(postId, 0, 10);

        assertNotNull(res);
        assertEquals(2, res.size());
    }

    @Test
    void 코멘트_가지고오기_조회_실패() {

        Long postId = 1L;

        when(postRepository.existsById(postId)).thenReturn(false);

        assertThrows(
                NotFoundException.class,
                () -> commentService.getComments(postId, 0, 10)
        );

        verify(commentRepository, never())
                .findByPost_PostIdOrderByCreateTimeAsc(anyLong(), any(Pageable.class));

    }


}