package com.studyCommunity.Community.service;

import com.studyCommunity.Community.dto.AttachmentDownloadStreamResult;
import com.studyCommunity.Community.entity.Attachment;
import com.studyCommunity.Community.entity.Post;
import com.studyCommunity.Community.exception.AttachmentUploadException;
import com.studyCommunity.Community.exception.BadRequestException;
import com.studyCommunity.Community.exception.ForbiddenException;
import com.studyCommunity.Community.exception.NotFoundException;
import com.studyCommunity.Community.infra.S3Uploader;
import com.studyCommunity.Community.repository.AttachmentRepository;
import com.studyCommunity.Community.type.AttachmentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceTest {

    @Mock
    private AttachmentRepository attachmentRepository;

    @Mock
    private S3Uploader s3Uploader;

    @InjectMocks
    private AttachmentService attachmentService;

    @Test
    void 업로드_files가_null() {

        List<Long> res = attachmentService.upload(null, "u1");
        assertNotNull(res);
        assertTrue(res.isEmpty());

        verifyNoInteractions(s3Uploader, attachmentRepository);

    }

    @Test
    void 업로드_files가_비어있을때() {

        List<Long> res = attachmentService.upload(List.of(), "u1");
        assertNotNull(res);
        assertTrue(res.isEmpty());

        verifyNoInteractions(s3Uploader, attachmentRepository);
    }

    @Test
    void 업로드_중간에실패시() {

        MultipartFile f1 = mock(MultipartFile.class);
        MultipartFile f2 = mock(MultipartFile.class);

        when(f1.isEmpty()).thenReturn(false);
        when(f2.isEmpty()).thenReturn(false);

        when(f1.getOriginalFilename()).thenReturn("a");
        when(f1.getSize()).thenReturn(10L);

        when(s3Uploader.upload(f1)).thenReturn("k1");
        when(attachmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        when(s3Uploader.upload(f2)).thenThrow(new RuntimeException("s3 fail"));

        assertThrows(AttachmentUploadException.class,
                () -> attachmentService.upload(List.of(f1,f2), "u1"));

        verify(s3Uploader).delete("k1");

    }

    @Test
    void 게시글첨부_id가_비면() {
        attachmentService.attachToPost(List.of(), mock(Post.class), "u1");
        verifyNoInteractions(attachmentRepository);
    }

    @Test
    void 게시글첨부_존재하지않는ID일시_실패() {
        List<Long> ids = List.of(1L, 2L);
        when(attachmentRepository.findAllById(ids))
                .thenReturn(List.of(mock(Attachment.class)));
        assertThrows(NotFoundException.class,
                () -> attachmentService.attachToPost(ids, mock(Post.class), "u1"));
    }

    @Test
    void 게시글첨부_TEMP아니면_실패() {
        List<Long> ids = List.of(1L);
        Post post = mock(Post.class);

        Attachment a = mock(Attachment.class);
        when(a.getUserId()).thenReturn("u1");
        when(a.getAttachmentStatus()).thenReturn(AttachmentStatus.ATTACHED);
        when(attachmentRepository.findAllById(ids)).thenReturn(List.of(a));

        assertThrows(ForbiddenException.class,
                () -> attachmentService.attachToPost(ids, post, "u1"));
    }

    @Test
    void 게시글첨부_정상() {
        List<Long> ids = List.of(1L, 2L);
        Post post = mock(Post.class);

        Attachment a1 = mock(Attachment.class);
        when(a1.getUserId()).thenReturn("u1");
        when(a1.getAttachmentStatus()).thenReturn(AttachmentStatus.TEMP);

        Attachment a2 = mock(Attachment.class);
        when(a2.getUserId()).thenReturn("u1");
        when(a2.getAttachmentStatus()).thenReturn(AttachmentStatus.TEMP);

        when(attachmentRepository.findAllById(ids)).thenReturn(List.of(a1, a2));

        attachmentService.attachToPost(ids, post, "u1");

        verify(a1).attachTo(post);
        verify(a2).attachTo(post);
    }


    @Test
    void 첨부삭제_ids비면_400() {
        assertThrows(BadRequestException.class,
                () -> attachmentService.deleteAttachmentByIds(List.of(), "u1"));
    }

    @Test
    void 첨부삭제_존재하지_않는_파일포함() {
        List<Long> ids = List.of(1L, 2L);
        when(attachmentRepository.findAllById(ids)).thenReturn(List.of(mock(Attachment.class)));

        assertThrows(NotFoundException.class,
                () -> attachmentService.deleteAttachmentByIds(ids, "u1"));

    }

    @Test
    void 첨부삭제_post없는경우_실패() {

        List<Long> ids = List.of(1L);

        Attachment a = mock(Attachment.class);
        when(a.getPost()).thenReturn(null);
        when(a.getUserId()).thenReturn("u1");

        when(attachmentRepository.findAllById(ids)).thenReturn(List.of(a));

        assertThrows(ForbiddenException.class,
                () -> attachmentService.deleteAttachmentByIds(ids, "other"));
    }
    @Test
    void 첨부삭제_정상_S3삭제후_DB삭제() {
        List<Long> ids = List.of(1L);

        Post post = mock(Post.class);
        when(post.getUserId()).thenReturn("writer");

        Attachment a = mock(Attachment.class);
        when(a.getPost()).thenReturn(post);
        when(a.getS3Key()).thenReturn("k1");

        when(attachmentRepository.findAllById(ids)).thenReturn(List.of(a));

        attachmentService.deleteAttachmentByIds(ids, "writer");

        verify(s3Uploader).delete("k1");
        verify(attachmentRepository).deleteAllInBatch(anyList());
    }

    @Test
    void 다운로드_없으면_404() {
        when(attachmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> attachmentService.download(1L));
    }

    @Test
    void 다운로드_첨부안된파일이면_403() {
        Attachment a = mock(Attachment.class);
        when(a.getAttachmentStatus()).thenReturn(AttachmentStatus.TEMP);

        when(attachmentRepository.findById(1L)).thenReturn(Optional.of(a));

        assertThrows(ForbiddenException.class,
                () -> attachmentService.download(1L));
    }

    @Test
    void 다운로드_정상() {
        Attachment a = mock(Attachment.class);
        when(a.getAttachmentStatus()).thenReturn(AttachmentStatus.ATTACHED);
        when(a.getS3Key()).thenReturn("k1");
        when(a.getOriginalFileName()).thenReturn("a.txt");

        when(attachmentRepository.findById(1L)).thenReturn(Optional.of(a));

        ResponseInputStream<GetObjectResponse> s3Stream = mock(ResponseInputStream.class);
        GetObjectResponse meta = GetObjectResponse.builder()
                .contentType("text/plain")
                .contentLength(10L)
                .build();

        when(s3Uploader.downloadStream("k1")).thenReturn(s3Stream);
        when(s3Stream.response()).thenReturn(meta);

        AttachmentDownloadStreamResult res = attachmentService.download(1L);

        assertEquals("a.txt", res.getOriginalFileName());
        assertEquals("text/plain", res.getContentType());
        assertEquals(10L, res.getContentLength());
        assertNotNull(res.getResource());
    }

}