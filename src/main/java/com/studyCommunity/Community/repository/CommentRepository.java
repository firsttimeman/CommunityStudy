package com.studyCommunity.Community.repository;

import com.studyCommunity.Community.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

}
