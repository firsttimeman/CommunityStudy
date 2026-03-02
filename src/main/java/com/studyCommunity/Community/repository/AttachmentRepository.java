package com.studyCommunity.Community.repository;

import com.studyCommunity.Community.entity.Attachment;
import com.studyCommunity.Community.entity.Post;
import com.studyCommunity.Community.type.AttachmentStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    List<Attachment> findAllByPost(Post post);

    @Query("""
            SELECT a
            from Attachment a
            where a.attachmentStatus = :status
            and a.expireAt < :now
            order by a.expireAt asc
            """)
    List<Attachment> findExpiredTempAttachments(
            @Param("status") AttachmentStatus status,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    List<Attachment> findAllByAttachmentIdInAndPost_PostId(List<Long> attachmentIds, Long postId);



    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Attachment a
           set a.post = :post,
               a.attachmentStatus = com.studyCommunity.Community.type.AttachmentStatus.ATTACHED,
               a.expireAt = null
         where a.attachmentId in :ids
           and a.userId = :userId
           and a.attachmentStatus = com.studyCommunity.Community.type.AttachmentStatus.TEMP
           and a.post is null
    """)
    int attachTempToPostAtomically(
            @Param("post") Post post,
            @Param("ids") List<Long> ids,
            @Param("userId") String userId
    );

}
