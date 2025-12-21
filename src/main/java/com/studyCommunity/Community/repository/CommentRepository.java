package com.studyCommunity.Community.repository;

import com.studyCommunity.Community.entity.Comment;
import com.studyCommunity.Community.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Modifying
    @Query("delete from Comment c where c.post = :post")
    void deleteAllByPost(@Param("post") Post post);

    List<Comment> findAllByPostOrderByCreateTimeAsc(Post post);
}
