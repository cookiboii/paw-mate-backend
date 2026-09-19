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

    // 전달받은 게시글 중 지정 회원이 북마크한 게시글 ID만 조회한다.
    @Query("SELECT b.post.id FROM PostBookmark b WHERE b.post.id IN :postIds AND b.member.id = :memberId")
    List<Long> findBookmarkedPostIds(@Param("postIds") List<Long> postIds, @Param("memberId") Long memberId);

    // 회원의 북마크를 최신 저장순으로 정렬해 게시글과 작성자 정보를 함께 커서 페이징으로 조회한다.
    @EntityGraph(attributePaths = "post.member")
    @Query("SELECT b.post FROM PostBookmark b WHERE b.member.id = :memberId ORDER BY b.createdAt DESC, b.id DESC")
    Slice<Post> findPostsByMemberIdOrderByCreatedAtDesc(@Param("memberId") Long memberId, Pageable pageable);
}
