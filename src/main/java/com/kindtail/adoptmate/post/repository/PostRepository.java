package com.kindtail.adoptmate.post.repository;

import com.kindtail.adoptmate.post.domain.Post;
import com.kindtail.adoptmate.post.domain.PostCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Override
    @EntityGraph(attributePaths = {"member"})
    Optional<Post> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"member"})
    Page<Post> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"member"})
    Slice<Post> findSliceBy(Pageable pageable);

    @EntityGraph(attributePaths = {"member"})
    @Query("SELECT p FROM Post p WHERE :lastPostId IS NULL OR (:lastCreatedAt IS NULL AND p.id < :lastPostId) OR (:lastCreatedAt IS NOT NULL AND (p.createdAt < :lastCreatedAt OR (p.createdAt = :lastCreatedAt AND p.id < :lastPostId))) ORDER BY p.createdAt DESC, p.id DESC")
    Slice<Post> findPostsByCursor(@Param("lastPostId") Long lastPostId,
                                  @Param("lastCreatedAt") LocalDateTime lastCreatedAt,
                                  Pageable pageable);

    @EntityGraph(attributePaths = {"member"})
    @Query("SELECT p FROM Post p WHERE (:category IS NULL OR p.category = :category) AND (:keyword IS NULL OR lower(p.title) LIKE lower(concat('%', :keyword, '%')) OR p.content LIKE concat('%', :keyword, '%') OR lower(p.member.name) LIKE lower(concat('%', :keyword, '%'))) AND (:lastPostId IS NULL OR (:lastCreatedAt IS NULL AND p.id < :lastPostId) OR (:lastCreatedAt IS NOT NULL AND (p.createdAt < :lastCreatedAt OR (p.createdAt = :lastCreatedAt AND p.id < :lastPostId)))) ORDER BY p.createdAt DESC, p.id DESC")
    Slice<Post> searchLatest(@Param("lastPostId") Long lastPostId,
                              @Param("lastCreatedAt") LocalDateTime lastCreatedAt,
                              @Param("category") PostCategory category,
                              @Param("keyword") String keyword,
                              Pageable pageable);

    @EntityGraph(attributePaths = {"member"})
    @Query("SELECT p FROM Post p WHERE (:category IS NULL OR p.category = :category) AND (:keyword IS NULL OR lower(p.title) LIKE lower(concat('%', :keyword, '%')) OR p.content LIKE concat('%', :keyword, '%') OR lower(p.member.name) LIKE lower(concat('%', :keyword, '%'))) AND (:lastPostId IS NULL OR (SELECT count(pl) FROM PostLike pl WHERE pl.post = p) < :lastLikeCount OR ((SELECT count(pl) FROM PostLike pl WHERE pl.post = p) = :lastLikeCount AND p.id < :lastPostId)) ORDER BY (SELECT count(pl) FROM PostLike pl WHERE pl.post = p) DESC, p.id DESC")
    Slice<Post> searchPopular(@Param("lastPostId") Long lastPostId,
                               @Param("lastLikeCount") long lastLikeCount,
                               @Param("category") PostCategory category,
                               @Param("keyword") String keyword,
                               Pageable pageable);

    @EntityGraph(attributePaths = {"member"})
    @Query("SELECT p FROM Post p WHERE (:category IS NULL OR p.category = :category) AND (:keyword IS NULL OR lower(p.title) LIKE lower(concat('%', :keyword, '%')) OR p.content LIKE concat('%', :keyword, '%') OR lower(p.member.name) LIKE lower(concat('%', :keyword, '%'))) AND (:lastPostId IS NULL OR (SELECT count(cm) FROM Comment cm WHERE cm.post = p) < :lastCommentCount OR ((SELECT count(cm) FROM Comment cm WHERE cm.post = p) = :lastCommentCount AND p.id < :lastPostId)) ORDER BY (SELECT count(cm) FROM Comment cm WHERE cm.post = p) DESC, p.id DESC")
    Slice<Post> searchByCommentCount(@Param("lastPostId") Long lastPostId,
                                      @Param("lastCommentCount") long lastCommentCount,
                                      @Param("category") PostCategory category,
                                      @Param("keyword") String keyword,
                                      Pageable pageable);
}
