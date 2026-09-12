package com.kindtail.adoptmate.comment.repository;

import com.kindtail.adoptmate.comment.domain.Comment;
import com.kindtail.adoptmate.post.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    long countByPostId(Long postId);

    @Query("SELECT c.post.id, count(c) FROM Comment c WHERE c.post.id IN :postIds GROUP BY c.post.id")
    List<Object[]> countByPostIds(@Param("postIds") List<Long> postIds);

    @EntityGraph(attributePaths = {"member"})
    List<Comment> findByPost(Post post);

    @EntityGraph(attributePaths = {"member", "children", "children.member"})
    List<Comment> findByPostAndParentIsNull(Post post);

    @EntityGraph(attributePaths = {"member"})
    Page<Comment> findByPostAndParentIsNull(Post post, Pageable pageable);
}
