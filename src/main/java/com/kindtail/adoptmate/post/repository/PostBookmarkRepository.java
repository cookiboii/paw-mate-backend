package com.kindtail.adoptmate.post.repository;

import com.kindtail.adoptmate.post.domain.Post;
import com.kindtail.adoptmate.post.domain.PostBookmark;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;

public interface PostBookmarkRepository extends JpaRepository<PostBookmark, Long> {
    Optional<PostBookmark> findByPostIdAndMemberId(Long postId, Long memberId);
    boolean existsByPostIdAndMemberId(Long postId, Long memberId);

    @Query("SELECT b.post.id FROM PostBookmark b WHERE b.post.id IN :postIds AND b.member.id = :memberId")
    List<Long> findBookmarkedPostIds(@Param("postIds") List<Long> postIds, @Param("memberId") Long memberId);

    @EntityGraph(attributePaths = "post.member")
    @Query("SELECT b.post FROM PostBookmark b WHERE b.member.id = :memberId ORDER BY b.createdAt DESC, b.id DESC")
    Slice<Post> findPostsByMemberIdOrderByCreatedAtDesc(@Param("memberId") Long memberId, Pageable pageable);
}
