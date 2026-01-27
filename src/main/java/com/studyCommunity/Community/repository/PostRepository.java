package com.studyCommunity.Community.repository;

import com.studyCommunity.Community.dto.PostListResponse;
import com.studyCommunity.Community.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("""
            select new com.studyCommunity.Community.dto.PostListResponse(
                p.postId,
                p.title,
                p.userId,
                p.createTime,
                (select count(c) from Comment c where c.post = p),
                (select count(a) from Attachment a where a.post = p)
            )
            from Post p
            order by p.createTime desc
            """)
    Page<PostListResponse> findPostList(Pageable pageable);
}
