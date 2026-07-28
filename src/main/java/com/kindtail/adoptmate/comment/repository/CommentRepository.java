package com.kindtail.adoptmate.comment.repository;

import com.kindtail.adoptmate.comment.domain.Comment;
import com.kindtail.adoptmate.post.domain.Post;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @EntityGraph(attributePaths = {"member"})
    List<Comment> findByPost(Post post);

    @EntityGraph(attributePaths = {"member", "children", "children.member"})
    List<Comment> findByPostAndParentIsNull(Post post);
}
