package com.studyCommunity.Community.repository;

import com.studyCommunity.Community.entity.Attachment;
import com.studyCommunity.Community.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    List<Attachment> findAllByPost(Post post);
}
