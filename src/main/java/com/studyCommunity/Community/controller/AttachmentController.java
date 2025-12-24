package com.studyCommunity.Community.controller;

import com.studyCommunity.Community.dto.AttachmentDownloadStreamResult;
import com.studyCommunity.Community.service.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping("/upload")
    public ResponseEntity<List<Long>> upload(
            @RequestAttribute("userId") String userId,
            @RequestPart("files") List<MultipartFile> files
    ) {
        List<Long> attachmentIds = attachmentService.upload(files, userId);
        return ResponseEntity.ok(attachmentIds);
    }



    //todo 다시 보기
    @GetMapping("/{attachmentId}/download")
    public ResponseEntity<Resource> download(@PathVariable Long attachmentId) {
        AttachmentDownloadStreamResult result = attachmentService.download(attachmentId);

        var builder = ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(safeContentType(result.getContentType())))
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition(result.getOriginalFileName()));

        if (result.getContentLength() > 0) {
            builder.contentLength(result.getContentLength());
        }

        return builder.body(result.getResource());
    }

    private String safeContentType(String ct) {
        return (ct == null || ct.isBlank())
                ? MediaType.APPLICATION_OCTET_STREAM_VALUE
                : ct;
    }

    private String contentDisposition(String filename) {
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        return "attachment; filename*=UTF-8''" + encoded;
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteByIds(
            @RequestAttribute("userId") String userId,
            @RequestBody List<Long> attachmentIds
    ) {
        attachmentService.deleteAttachmentByIds(attachmentIds, userId);
        return ResponseEntity.noContent().build();
    }
}
