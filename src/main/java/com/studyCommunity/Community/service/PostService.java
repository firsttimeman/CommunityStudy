package com.studyCommunity.Community.service;

import com.studyCommunity.Community.dto.PostRequest;
import com.studyCommunity.Community.entity.Attachment;
import com.studyCommunity.Community.entity.Post;
import com.studyCommunity.Community.repository.AttachmentRepository;
import com.studyCommunity.Community.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final AttachmentRepository attachmentRepository;


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


        List<Attachment> id = attachmentRepository.findAllById(request.getAttachmentIds());

        for (Attachment attachment : id) {
            attachment.attachedTo(post);
        }

        return post.getPostId();
    }

}
