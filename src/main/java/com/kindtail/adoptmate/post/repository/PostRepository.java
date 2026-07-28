package com.kindtail.adoptmate.post.repository;

import com.kindtail.adoptmate.post.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Override
    @EntityGraph(attributePaths = {"member"})
    Optional<Post> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"member"})
    Page<Post> findAll(Pageable pageable);
}
