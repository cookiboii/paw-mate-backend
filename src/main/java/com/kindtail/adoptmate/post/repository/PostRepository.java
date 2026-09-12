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
    @Query("SELECT p FROM Post p WHERE (:lastPostId IS NULL OR ((SELECT c.createdAt FROM Post c WHERE c.id = :lastPostId) IS NULL AND p.id < :lastPostId) OR (p.createdAt < (SELECT c.createdAt FROM Post c WHERE c.id = :lastPostId) OR (p.createdAt = (SELECT c.createdAt FROM Post c WHERE c.id = :lastPostId) AND p.id < :lastPostId))) ORDER BY p.createdAt DESC, p.id DESC")
    Slice<Post> findPostsByCursor(@Param("lastPostId") Long lastPostId, Pageable pageable);

    @EntityGraph(attributePaths = {"member"})
    @Query("SELECT p FROM Post p WHERE (:category IS NULL OR p.category = :category) AND (:keyword IS NULL OR lower(p.title) LIKE lower(concat('%', :keyword, '%')) OR p.content LIKE concat('%', :keyword, '%') OR lower(p.member.name) LIKE lower(concat('%', :keyword, '%'))) AND (:lastPostId IS NULL OR ((SELECT c.createdAt FROM Post c WHERE c.id = :lastPostId) IS NULL AND p.id < :lastPostId) OR (p.createdAt < (SELECT c.createdAt FROM Post c WHERE c.id = :lastPostId) OR (p.createdAt = (SELECT c.createdAt FROM Post c WHERE c.id = :lastPostId) AND p.id < :lastPostId))) ORDER BY p.createdAt DESC, p.id DESC")
    Slice<Post> searchLatest(@Param("lastPostId") Long lastPostId, @Param("category") PostCategory category, @Param("keyword") String keyword, Pageable pageable);

    @EntityGraph(attributePaths = {"member"})
    @Query("SELECT p FROM Post p WHERE (:category IS NULL OR p.category = :category) AND (:keyword IS NULL OR lower(p.title) LIKE lower(concat('%', :keyword, '%')) OR p.content LIKE concat('%', :keyword, '%') OR lower(p.member.name) LIKE lower(concat('%', :keyword, '%'))) AND (:lastPostId IS NULL OR ((SELECT count(pl) FROM PostLike pl WHERE pl.post = p) < (SELECT count(cl) FROM PostLike cl WHERE cl.post.id = :lastPostId) OR ((SELECT count(pl) FROM PostLike pl WHERE pl.post = p) = (SELECT count(cl) FROM PostLike cl WHERE cl.post.id = :lastPostId) AND p.id < :lastPostId))) ORDER BY (SELECT count(pl) FROM PostLike pl WHERE pl.post = p) DESC, p.id DESC")
    Slice<Post> searchPopular(@Param("lastPostId") Long lastPostId, @Param("category") PostCategory category, @Param("keyword") String keyword, Pageable pageable);

    @EntityGraph(attributePaths = {"member"})
    @Query("SELECT p FROM Post p WHERE (:category IS NULL OR p.category = :category) AND (:keyword IS NULL OR lower(p.title) LIKE lower(concat('%', :keyword, '%')) OR p.content LIKE concat('%', :keyword, '%') OR lower(p.member.name) LIKE lower(concat('%', :keyword, '%'))) AND (:lastPostId IS NULL OR ((SELECT count(cm) FROM Comment cm WHERE cm.post = p) < (SELECT count(cursorComment) FROM Comment cursorComment WHERE cursorComment.post.id = :lastPostId) OR ((SELECT count(cm) FROM Comment cm WHERE cm.post = p) = (SELECT count(cursorComment) FROM Comment cursorComment WHERE cursorComment.post.id = :lastPostId) AND p.id < :lastPostId))) ORDER BY (SELECT count(cm) FROM Comment cm WHERE cm.post = p) DESC, p.id DESC")
    Slice<Post> searchByCommentCount(@Param("lastPostId") Long lastPostId, @Param("category") PostCategory category, @Param("keyword") String keyword, Pageable pageable);
}
