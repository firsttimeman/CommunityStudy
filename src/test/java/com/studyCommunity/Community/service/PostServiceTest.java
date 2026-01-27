package com.studyCommunity.Community.service;

import com.studyCommunity.Community.dto.PostDetailResponse;
import com.studyCommunity.Community.dto.PostListResponse;
import com.studyCommunity.Community.dto.PostRequest;
import com.studyCommunity.Community.dto.PostUpdateRequest;
import com.studyCommunity.Community.entity.Attachment;
import com.studyCommunity.Community.entity.Comment;
import com.studyCommunity.Community.entity.Post;
import com.studyCommunity.Community.exception.BadRequestException;
import com.studyCommunity.Community.exception.ForbiddenException;
import com.studyCommunity.Community.exception.NotFoundException;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock PostRepository postRepository;
    @Mock AttachmentRepository attachmentRepository;
    @Mock AttachmentService attachmentService;
    @Mock CommentRepository commentRepository;

    @InjectMocks PostService postService;


    @Test
    void 게시글생성_첨부없으면_save() {

        PostRequest request = new PostRequest("t", "c", List.of());
        String userId = "u1";

        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));

        postService.createPost(request, userId);
        verify(attachmentService, never()).attachToPost(anyList(), any(Post.class), anyString());
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    void 게시글생성_첨부있으면_attachToPost() {

        List<Long> attachmentIds = List.of(1L, 2L);
        PostRequest request = new PostRequest("t", "c", attachmentIds);
        String userId = "u1";

        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));

        postService.createPost(request, userId);

        ArgumentCaptor<Post> postArgumentCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postArgumentCaptor.capture());

        Post savedPost = postArgumentCaptor.getValue();
        verify(attachmentService).attachToPost(eq(attachmentIds), eq(savedPost), eq(userId));
    }

    @Test
    void 게시글생성_첨부연결중_예외발생시() {
        List<Long> attachmentIds = List.of(1L);
        PostRequest request = new PostRequest("t", "c", attachmentIds);
        String userId = "u1";

        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));

        doThrow(new RuntimeException("attach 실패"))
                .when(attachmentService)
                .attachToPost(anyList(), any(Post.class), anyString());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> postService.createPost(request, userId));

        assertEquals("attach 실패", ex.getMessage());
    }


    @Test
    void 게시글수정_정상() {
        Long postId = 1L;
        String userId = "u1";
        PostUpdateRequest  request = new PostUpdateRequest("nt", "nc");

        Post post = mock(Post.class);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(post.getUserId()).thenReturn(userId);

        postService.updatePost(request, userId, postId);

        verify(post).update("nt", "nc");

    }
    @Test
    void 게시글수정_post없음() {
        Long postId = 1L;
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> postService.updatePost(new PostUpdateRequest("t", "c"), "u1", postId));

        verifyNoInteractions(attachmentService, attachmentRepository, commentRepository);
    }

    @Test
    void 게시글수정_본인아님() {
        Long postId = 1L;
        String userId = "u1";
        PostUpdateRequest  request = new PostUpdateRequest("nt", "nc");

        Post post = mock(Post.class);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(post.getUserId()).thenReturn("other");

        assertThrows(ForbiddenException.class,
                () -> postService.updatePost(request, userId, postId));

        verify(post, never()).update("nt", "nc");
    }

    @Test
    void 게시글삭제_정상() {
        Long postId = 1L;
        String userId = "u1";

        Post post = mock(Post.class);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(post.getUserId()).thenReturn(userId);

        postService.deletePost(postId, userId);

        InOrder inOrder = inOrder(attachmentService, commentRepository, postRepository);
        inOrder.verify(attachmentService).deleteAllPost(post);
        inOrder.verify(commentRepository).deleteAllByPost(post);
        inOrder.verify(postRepository).delete(post);

    }

    @Test
    void 게시글삭제_post가_없을떄() {
        Long postId = 1L;
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> postService.deletePost(postId, "u1"));

        verifyNoInteractions(attachmentService, commentRepository);
        verify(postRepository, never()).delete(any());
    }

    @Test
    void 게시글삭제_작성자아님() {
        Long postId = 1L;
        String userId = "u1";

        Post post = mock(Post.class);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(post.getUserId()).thenReturn("other");

        assertThrows(ForbiddenException.class,
                () -> postService.deletePost(postId, userId));

        verifyNoInteractions(attachmentService, commentRepository);
        verify(postRepository, never()).delete(any());
    }

    @Test // todo 다시 체크하기
    void 게시글조회_정상() {
        int page = 2;
        int size = 5;

        PostListResponse dto1 = mock(PostListResponse.class);
        PostListResponse dto2 = mock(PostListResponse.class);

        PageImpl<PostListResponse> fakePage =
                new PageImpl<>(List.of(dto1, dto2), PageRequest.of(page, size), 12); // todo 체크하기

        when(postRepository.findPostList(any(Pageable.class))).thenReturn(fakePage);

        Page<PostListResponse> result = postService.getPostList(page, size);

        assertNotNull(result);
        assertEquals(12, result.getTotalElements());
        assertEquals(2, result.getContent().size());

        ArgumentCaptor<Pageable> pageableArgumentCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(postRepository).findPostList(pageableArgumentCaptor.capture());


        Pageable used = pageableArgumentCaptor.getValue();
        assertEquals(page, used.getPageNumber());
        assertEquals(size, used.getPageSize());

    }

    @Test
    void 게시글목록조회_음수라면() {
        assertThrows(IllegalArgumentException.class,
                () -> postService.getPostList(-1, 10));

        verifyNoInteractions(postRepository);
    }


    @Test
    void 게시글상세조회_정상_첨부파일포함() {
        Long postId = 1L;

        Post post = mock(Post.class);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(post.getPostId()).thenReturn(postId);
        when(post.getTitle()).thenReturn("t");
        when(post.getContent()).thenReturn("c");
        when(post.getUserId()).thenReturn("u1");
        when(post.getCreateTime()).thenReturn(LocalDateTime.of(2026, 1, 1, 0, 0));


        Attachment a1 = mock(Attachment.class);
        when(a1.getAttachmentId()).thenReturn(10L);
        when(a1.getOriginalFileName()).thenReturn("a.txt");
        when(a1.getS3Key()).thenReturn("k1");
        when(attachmentRepository.findAllByPost(post)).thenReturn(List.of(a1));


        Comment c1 = mock(Comment.class);
        when(c1.getCommentId()).thenReturn(100L);
        when(c1.getContent()).thenReturn("hello");
        when(c1.getUserId()).thenReturn("u2");
        when(c1.getCreateTime()).thenReturn(LocalDateTime.of(2026, 1, 1, 1, 0));
        when(commentRepository.findAllByPostOrderByCreateTimeAsc(post)).thenReturn(List.of(c1));

        PostDetailResponse res = postService.getPostDetail(postId);

        assertNotNull(res);
        assertEquals(postId, res.getPostId());
        assertEquals("t", res.getTitle());
        assertEquals("c", res.getContent());
        assertEquals("u1", res.getUserId());

        assertEquals(1, res.getAttachments().size());
        assertEquals(10L, res.getAttachments().get(0).getAttachmentId());
        assertEquals("a.txt", res.getAttachments().get(0).getOriginalFileName());

        assertEquals(1, res.getComments().size());
        assertEquals(100L, res.getComments().get(0).getCommentId());
        assertEquals("hello", res.getComments().get(0).getContent());
        assertEquals("u2", res.getComments().get(0).getUserId());


    }

    @Test
    void 게시글상세조회_포스트없음() {
        when(postRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> postService.getPostDetail(anyLong()));

        verifyNoInteractions(attachmentRepository, commentRepository);
    }

    @Test
    void 첨부추가_정상() {
        Long postId = 1L;
        String userId = "u1";
        List<Long> attachmentIds = List.of(1L, 2L);

        Post post = mock(Post.class);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(post.getUserId()).thenReturn(userId);

        postService.addAttachments(postId, attachmentIds, userId);

        verify(attachmentService).attachToPost(attachmentIds, post, userId);

    }

    @Test
    void 첨부추가_post없음_NotFound() {
        // given
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class,
                () -> postService.addAttachments(1L, List.of(1L), "u1"));

        verifyNoInteractions(attachmentService);
    }

    @Test
    void 첨부추가_작성자아님_Forbidden() {
        // given
        Long postId = 1L;
        Post post = mock(Post.class);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(post.getUserId()).thenReturn("other");

        // when & then
        assertThrows(ForbiddenException.class,
                () -> postService.addAttachments(postId, List.of(1L), "u1"));

        verifyNoInteractions(attachmentService);
    }

    @Test
    void 첨부추가_ids비었으면_BadRequest() {
        // given
        Long postId = 1L;
        String userId = "u1";

        Post post = mock(Post.class);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(post.getUserId()).thenReturn(userId);

        // when & then
        assertThrows(BadRequestException.class,
                () -> postService.addAttachments(postId, List.of(), userId));

        verifyNoInteractions(attachmentService);
    }





}