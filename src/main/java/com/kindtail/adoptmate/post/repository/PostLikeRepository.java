package com.kindtail.adoptmate.post.repository;

import com.kindtail.adoptmate.post.domain.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    Optional<PostLike> findByPostIdAndMemberId(Long postId, Long memberId);
    boolean existsByPostIdAndMemberId(Long postId, Long memberId);
    long countByPostId(Long postId);

    // 전달받은 게시글별 좋아요 수를 한 번에 집계해 [게시글 ID, 좋아요 수] 형태로 반환한다.
    @Query("SELECT pl.post.id, count(pl) FROM PostLike pl WHERE pl.post.id IN :postIds GROUP BY pl.post.id")
    List<Object[]> countByPostIds(@Param("postIds") List<Long> postIds);

    // 전달받은 게시글 중 지정 회원이 좋아요한 게시글 ID만 조회한다.
    @Query("SELECT pl.post.id FROM PostLike pl WHERE pl.post.id IN :postIds AND pl.member.id = :memberId")
    List<Long> findLikedPostIds(@Param("postIds") List<Long> postIds, @Param("memberId") Long memberId);
}
