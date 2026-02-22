package com.studyCommunity.Community.entity;

import com.studyCommunity.Community.exception.BadRequestException;
import com.studyCommunity.Community.exception.ForbiddenException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
//@Table(name = "comments",
//        indexes = {
//            @Index(name = "idx_comment_postid", columnList = "post_id")
//        }
//)
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    public static Comment createComment(String content, String userId, Post post) {

        return Comment.builder()
                .content(content)
                .userId(userId)
                .post(post)
                .build();
    }

    public void updateComment(String content, String userId) {
        if (!this.userId.equals(userId)) {
            throw new ForbiddenException("댓글 작성자만 수정할 수 있습니다.");
        }
        this.content = content;
    }


}
