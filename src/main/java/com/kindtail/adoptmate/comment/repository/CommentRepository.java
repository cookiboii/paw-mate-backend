package com.kindtail.adoptmate.comment.repository;

import com.kindtail.adoptmate.comment.domain.Comment;
import com.kindtail.adoptmate.post.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPost(Post post);
}
