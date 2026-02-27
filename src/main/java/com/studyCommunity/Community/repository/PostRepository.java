package com.studyCommunity.Community.repository;

import com.studyCommunity.Community.dto.PostListResponse;
import com.studyCommunity.Community.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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


//    @Query(
//            value = """
//    select new com.studyCommunity.Community.dto.PostListResponse(
//        p.postId,
//        p.title,
//        p.userId,
//        p.createTime,
//        count(distinct c.commentId),
//        count(distinct a.attachmentId)
//    )
//    from Post p
//    left join Comment c on c.post = p
//    left join Attachment a on a.post = p
//    group by p.postId, p.title, p.userId, p.createTime
//    order by count(distinct c.commentId) desc, p.postId desc
//  """,
//            countQuery = """
//    select count(p.postId)
//    from Post p
//  """
//    )
//    Page<PostListResponse> findPopularPostListHeavy(Pageable pageable);


    @Query("""
  select new com.studyCommunity.Community.dto.PostListResponse(
    p.postId, p.title, p.userId, p.createTime,
    p.commentCount, p.attachmentCount
  )
  from Post p
  order by p.commentCount desc, p.postId desc
""")
    Page<PostListResponse> findPopularPostList(Pageable pageable);


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Post p set p.commentCount = p.commentCount + 1 where p.postId = :postId")
    int incrementCommentCount(@Param("postId") Long postId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Post p
           set p.commentCount = case when p.commentCount > 0 then p.commentCount - 1 else 0 end
         where p.postId = :postId
    """)
    int decrementCommentCount(@Param("postId") Long postId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Post p set p.attachmentCount = p.attachmentCount + :n where p.postId = :postId")
    int increaseAttachmentCount(@Param("postId") Long postId, @Param("n") long n);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Post p
           set p.attachmentCount =
               case when p.attachmentCount >= :n then p.attachmentCount - :n else 0 end
         where p.postId = :postId
    """)
    int decreaseAttachmentCount(@Param("postId") Long postId, @Param("n") long n);


}
