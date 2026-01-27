//package com.studyCommunity.Community.controller;
//
//import com.studyCommunity.Community.dto.*;
//import com.studyCommunity.Community.service.PostService;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.restdocs.test.autoconfigure.AutoConfigureRestDocs;
//import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.http.MediaType;
//import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.servlet.MockMvc;
//import tools.jackson.databind.ObjectMapper;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.doNothing;
//import static org.mockito.Mockito.when;
//import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
//import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
//import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
//import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
//
//import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
//import static org.springframework.restdocs.payload.PayloadDocumentation.*;
//
//
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//import static org.springframework.restdocs.request.RequestDocumentation.*;
//
//
//@WebMvcTest(PostController.class)
//@AutoConfigureRestDocs(outputDir = "build/generated-snippets")
//class PostControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//    @Autowired
//    private ObjectMapper objectMapper;
//    @MockitoBean
//    private PostService postService;
//
//
//
//    @Test
//    void createPost() throws Exception {
//        PostRequest request = new PostRequest("t", "c", List.of());
//
//        when(postService.createPost(any(PostRequest.class), eq("user-1")))
//                .thenReturn(1L);
//
//        mockMvc.perform(post("/post")
//                        .header("X-User-Id", "user-1")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andDo(document("post-create",
//                        requestHeaders(
//                                headerWithName("X-User-Id").description("요청 사용자 ID")
//                        ),
//                        requestFields(
//                                fieldWithPath("title").description("게시글 제목"),
//                                fieldWithPath("content").description("게시글 내용"),
//                                fieldWithPath("attachmentIds").description("첨부파일 ID 목록 (없으면 빈 배열)")
//                        ),
//                        responseFields(
//                                fieldWithPath("postId").description("생성된 게시글 ID")
//                        )
//                ));
//    }
//
//    @Test
//    void updatePost() throws Exception {
//        Long postId = 1L;
//        PostUpdateRequest request = new PostUpdateRequest("nt", "nc");
//
//        doNothing().when(postService).updatePost(any(PostUpdateRequest.class), eq("user-1"), eq(postId));
//
//        mockMvc.perform(put("/post/{postId}", postId)
//                .header("X-User-Id", "user-1")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andDo(document("post-update",
//                        requestHeaders(
//                                headerWithName("X-User-Id").description("요청 사용자 ID")
//                        ),
//                        pathParameters(
//                                parameterWithName("postId").description("수정할 게시글 ID")
//                        ),
//                        requestFields(
//                                fieldWithPath("title").description("수정할 게시글 제목"),
//                                fieldWithPath("content").description("수정할 게시글 내용")
//                        )
//                ));
//
//    }
//
//
//    @Test
//    void deletePost() throws Exception {
//        Long postId = 1L;
//
//        doNothing().when(postService).deletePost(eq(postId), eq("user-1"));
//
//        mockMvc.perform(
//                RestDocumentationRequestBuilders
//                        .delete("/post/{postId}", postId)
//                        .header("X-User-Id", "user-1")
//        )
//                .andExpect(status().isOk())
//                .andDo(document("post-delete",
//                        requestHeaders(
//                                headerWithName("X-User-Id").description("요청 사용자 ID")
//                        ),
//                        pathParameters(
//                                parameterWithName("postId").description("삭제할 게시글 ID")
//                        )
//                ));
//    }
//
//    @Test //todo 나중에 살펴보기
//    void getPostList() throws Exception {
//
//        int page = 0;
//        int size = 20;
//
//        LocalDateTime t1 = LocalDateTime.of(2025, 12, 22, 10, 0, 0);
//        LocalDateTime t2 = LocalDateTime.of(2025, 12, 22, 11, 0, 0);
//
//        PageImpl<PostListResponse> pageResult = new PageImpl<>(
//                List.of(
//                        new PostListResponse(1L, "제목1", "user-1", t1, 3L, 1L),
//                        new PostListResponse(2L, "제목2", "user-2", t2, 0L, 0L)
//                ),
//                PageRequest.of(page, size),
//                2
//        );
//
//        when(postService.getPostList(page, size)).thenReturn(pageResult);
//
//        mockMvc.perform(
//                        RestDocumentationRequestBuilders.get("/post")
//                                .header("X-User-Id", "user-1")
//                                .param("page", String.valueOf(page))
//                                .param("size", String.valueOf(size))
//                                .accept(MediaType.APPLICATION_JSON)
//                )
//                .andExpect(status().isOk())
//                .andDo(document("post-list",
//                        queryParameters(
//                                parameterWithName("page").description("페이지 번호 (0부터 시작, 기본값 0)"),
//                                parameterWithName("size").description("페이지 크기 (기본값 20)")
//                        ),
//                        responseFields(
//                                // content
//                                fieldWithPath("content[].postId").description("게시글 ID"),
//                                fieldWithPath("content[].title").description("게시글 제목"),
//                                fieldWithPath("content[].userId").description("작성자 ID"),
//                                fieldWithPath("content[].createdAt").description("작성 시각"),
//                                fieldWithPath("content[].commentCount").description("댓글 수"),
//                                fieldWithPath("content[].attachmentCount").description("첨부파일 수"),
//
//                                // pageable
//                                fieldWithPath("pageable.sort.empty").description("정렬 정보 비어있는지 여부"),
//                                fieldWithPath("pageable.sort.sorted").description("정렬 적용 여부"),
//                                fieldWithPath("pageable.sort.unsorted").description("정렬 미적용 여부"),
//                                fieldWithPath("pageable.offset").description("오프셋"),
//                                fieldWithPath("pageable.pageNumber").description("현재 페이지 번호"),
//                                fieldWithPath("pageable.pageSize").description("페이지 크기"),
//                                fieldWithPath("pageable.paged").description("페이징 여부"),
//                                fieldWithPath("pageable.unpaged").description("언페이징 여부"),
//
//                                // sort (top-level)
//                                fieldWithPath("sort.empty").description("정렬 정보 비어있는지 여부"),
//                                fieldWithPath("sort.sorted").description("정렬 적용 여부"),
//                                fieldWithPath("sort.unsorted").description("정렬 미적용 여부"),
//
//                                // page meta
//                                fieldWithPath("last").description("마지막 페이지 여부"),
//                                fieldWithPath("totalPages").description("전체 페이지 수"),
//                                fieldWithPath("totalElements").description("전체 요소 수"),
//                                fieldWithPath("size").description("요청한 페이지 크기"),
//                                fieldWithPath("number").description("현재 페이지 번호"),
//                                fieldWithPath("first").description("첫 페이지 여부"),
//                                fieldWithPath("numberOfElements").description("현재 페이지에 담긴 요소 수"),
//                                fieldWithPath("empty").description("content가 비어있는지 여부")
//                        )
//                ));
//
//    }
//
//    @Test
//    void getPostDetail() throws Exception {
//
//        Long postId = 1L;
//        LocalDateTime createdAt =
//                LocalDateTime.of(2025, 12, 22, 10, 30, 0);
//
//        when(postService.getPostDetail(postId)).thenReturn(
//                new PostDetailResponse(
//                        postId,
//                        "게시글 제목",
//                        "게시글 내용",
//                        "user-1",
//                        createdAt,
//                        List.of(
//                                new AttachmentResponse(
//                                        1L,
//                                        "image.png",
//                                        "post/1/image.png"
//                                )
//                        ),
//                        List.of(
//                                new CommentResponse(
//                                        1L,
//                                        "댓글 내용",
//                                        "user-2",
//                                        createdAt
//                                )
//                        )
//                )
//        );
//
//        mockMvc.perform(
//                RestDocumentationRequestBuilders.get("/post/{postId}", postId)
//                        .header("X-User-Id", "user-1")
//                        .accept(MediaType.APPLICATION_JSON)
//        )
//                .andExpect(status().isOk())
//                .andDo(document("post-detail",
//                        pathParameters(
//                                parameterWithName("postId")
//                                        .description("조회할 게시글 ID")
//                        ),
//                        responseFields(
//                                fieldWithPath("postId").description("게시글 ID"),
//                                fieldWithPath("title").description("게시글 제목"),
//                                fieldWithPath("content").description("게시글 내용"),
//                                fieldWithPath("userId").description("작성자 ID"),
//                                fieldWithPath("createdAt").description("게시글 생성 시각"),
//
//                                fieldWithPath("attachments[].attachmentId").description("첨부파일 ID"),
//                                fieldWithPath("attachments[].originalFileName").description("원본 파일명"),
//                                fieldWithPath("attachments[].s3Key").description("S3 저장 키"),
//
//                                fieldWithPath("comments[].commentId").description("댓글 ID"),
//                                fieldWithPath("comments[].content").description("댓글 내용"),
//                                fieldWithPath("comments[].userId").description("댓글 작성자 ID"),
//                                fieldWithPath("comments[].createdAt").description("댓글 생성 시각")
//                        )
//                ));
//    }
//
//    }
