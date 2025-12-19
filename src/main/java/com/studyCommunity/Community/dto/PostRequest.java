package com.studyCommunity.Community.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@NoArgsConstructor
public class PostRequest {

    private String title;
    private String content;
    private List<Long> attachmentIds;


}
