package com.kindtail.adoptmate.post.repository;

import com.kindtail.adoptmate.post.domain.Post;
import com.kindtail.adoptmate.post.domain.PostCategory;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 게시글 수정·삭제 등 동시 변경을 막기 위해 해당 행을 비관적 쓰기 잠금으로 조회한다.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Post p WHERE p.id = :id")
    Optional<Post> findByIdForUpdate(@Param("id") Long id);

    @Override
    @EntityGraph(attributePaths = {"member"})
    Optional<Post> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"member"})
    Page<Post> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"member"})
    @Query("SELECT p FROM Post p WHERE :lastPostId IS NULL OR (:lastCreatedAt IS NULL AND p.id < :lastPostId) OR (:lastCreatedAt IS NOT NULL AND (p.createdAt < :lastCreatedAt OR (p.createdAt = :lastCreatedAt AND p.id < :lastPostId))) ORDER BY p.createdAt DESC, p.id DESC")
    // 최신순(createdAt DESC, id DESC) 목록을 위한 커서 페이징 쿼리이다.
    // 마지막 게시글의 생성 시각과 ID보다 이전인 게시글만 조회하며, 첫 페이지에서는 커서 조건을 적용하지 않는다.
    Slice<Post> findPostsByCursor(@Param("lastPostId") Long lastPostId,
                                  @Param("lastCreatedAt") LocalDateTime lastCreatedAt,
                                  Pageable pageable);

    @EntityGraph(attributePaths = {"member"})
    @Query("SELECT p FROM Post p WHERE (:category IS NULL OR p.category = :category) AND (:keyword IS NULL OR lower(p.title) LIKE lower(concat('%', :keyword, '%')) OR p.content LIKE concat('%', :keyword, '%') OR lower(p.member.name) LIKE lower(concat('%', :keyword, '%'))) AND (:lastPostId IS NULL OR (:lastCreatedAt IS NULL AND p.id < :lastPostId) OR (:lastCreatedAt IS NOT NULL AND (p.createdAt < :lastCreatedAt OR (p.createdAt = :lastCreatedAt AND p.id < :lastPostId)))) ORDER BY p.createdAt DESC, p.id DESC")
    // 카테고리와 제목·내용·작성자명 키워드를 선택적으로 필터링한 뒤 최신순으로 커서 페이징한다.
    // 생성 시각이 같은 게시글은 ID를 보조 커서로 사용해 중복·누락 없이 다음 페이지를 조회한다.
    Slice<Post> searchLatest(@Param("lastPostId") Long lastPostId,
                              @Param("lastCreatedAt") LocalDateTime lastCreatedAt,
                              @Param("category") PostCategory category,
                              @Param("keyword") String keyword,
                              Pageable pageable);

    @EntityGraph(attributePaths = {"member"})
    @Query("SELECT p FROM Post p WHERE (:category IS NULL OR p.category = :category) AND (:keyword IS NULL OR lower(p.title) LIKE lower(concat('%', :keyword, '%')) OR p.content LIKE concat('%', :keyword, '%') OR lower(p.member.name) LIKE lower(concat('%', :keyword, '%'))) AND (:lastPostId IS NULL OR (SELECT count(pl) FROM PostLike pl WHERE pl.post = p) < :lastLikeCount OR ((SELECT count(pl) FROM PostLike pl WHERE pl.post = p) = :lastLikeCount AND p.id < :lastPostId)) ORDER BY (SELECT count(pl) FROM PostLike pl WHERE pl.post = p) DESC, p.id DESC")
    // 카테고리·키워드 검색 결과를 좋아요 수 내림차순으로 조회한다.
    // 마지막 좋아요 수보다 적거나, 같은 좋아요 수에서 ID가 더 작은 게시글을 다음 페이지로 가져온다.
    Slice<Post> searchPopular(@Param("lastPostId") Long lastPostId,
                               @Param("lastLikeCount") long lastLikeCount,
                               @Param("category") PostCategory category,
                               @Param("keyword") String keyword,
                               Pageable pageable);

    @EntityGraph(attributePaths = {"member"})
    @Query("SELECT p FROM Post p WHERE (:category IS NULL OR p.category = :category) AND (:keyword IS NULL OR lower(p.title) LIKE lower(concat('%', :keyword, '%')) OR p.content LIKE concat('%', :keyword, '%') OR lower(p.member.name) LIKE lower(concat('%', :keyword, '%'))) AND (:lastPostId IS NULL OR (SELECT count(cm) FROM Comment cm WHERE cm.post = p) < :lastCommentCount OR ((SELECT count(cm) FROM Comment cm WHERE cm.post = p) = :lastCommentCount AND p.id < :lastPostId)) ORDER BY (SELECT count(cm) FROM Comment cm WHERE cm.post = p) DESC, p.id DESC")
    // 카테고리·키워드 검색 결과를 댓글 수 내림차순으로 조회한다.
    // 마지막 댓글 수보다 적거나, 같은 댓글 수에서 ID가 더 작은 게시글을 다음 페이지로 가져온다.
    Slice<Post> searchByCommentCount(@Param("lastPostId") Long lastPostId,
                                      @Param("lastCommentCount") long lastCommentCount,
                                      @Param("category") PostCategory category,
                                      @Param("keyword") String keyword,
                                      Pageable pageable);
}
