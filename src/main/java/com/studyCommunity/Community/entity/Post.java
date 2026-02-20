package com.studyCommunity.Community.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "post",
        indexes = {
            @Index(name = "idx_post_commentcount_postid", columnList = "comment_count, post_id"), // todo postid
                @Index(name = "idx_post_createtime_postid", columnList = "create_time, post_id") // createtime
        }
)
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private long commentCount = 0;

    @Column(nullable = false)
    private long attachmentCount = 0;


    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void incrementCommentCount() {
        this.commentCount++;
    }

    public void decrementCommentCount() {
        if (this.commentCount > 0) this.commentCount--;
    }

    public void increaseAttachmentCount(long n) {
        this.attachmentCount += n;
    }

    public void decreaseAttachmentCount(long n) {
        this.attachmentCount = Math.max(0, this.attachmentCount - n);
    }


}
