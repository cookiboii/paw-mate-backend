package com.kindtail.adoptmate.post.repository;

import com.kindtail.adoptmate.post.domain.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    Optional<PostLike> findByPostIdAndMemberId(Long postId, Long memberId);
    boolean existsByPostIdAndMemberId(Long postId, Long memberId);
    long countByPostId(Long postId);
}
