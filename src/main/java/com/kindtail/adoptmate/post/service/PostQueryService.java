package com.kindtail.adoptmate.post.service;

import com.kindtail.adoptmate.auth.CurrentUserProvider;
import com.kindtail.adoptmate.common.exception.CustomException;
import com.kindtail.adoptmate.common.exception.ErrorCode;
import com.kindtail.adoptmate.comment.repository.CommentRepository;
import com.kindtail.adoptmate.post.domain.Post;
import com.kindtail.adoptmate.post.dto.PostResponse;
import com.kindtail.adoptmate.post.repository.PostBookmarkRepository;
import com.kindtail.adoptmate.post.repository.PostLikeRepository;
import com.kindtail.adoptmate.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Read-side use cases for posts, including batched counts and current-user flags. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostQueryService {
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostBookmarkRepository postBookmarkRepository;
    private final CommentRepository commentRepository;
    private final CurrentUserProvider currentUserProvider;

    public Page<PostResponse> getAllPosts(Pageable pageable) {
        Page<Post> posts = postRepository.findAll(pageable);
        Long memberId = currentUserProvider.optionalCurrentUserId().orElse(null);
        return new PageImpl<>(toResponses(posts.getContent(), memberId), posts.getPageable(), posts.getTotalElements());
    }

    public Slice<PostResponse> getPostsByCursor(Long lastPostId, int size) {
        Slice<Post> posts = postRepository.findPostsByCursor(lastPostId, cursorCreatedAt(lastPostId), PageRequest.of(0, size));
        return slice(posts, currentUserProvider.optionalCurrentUserId().orElse(null));
    }

    public Slice<PostResponse> searchPosts(Long lastPostId, int size, String keyword, String sort) {
        validateSize(size);
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
        String normalizedSort = sort == null || sort.isBlank() ? "latest" : sort.trim().toLowerCase();
        Pageable pageable = PageRequest.of(0, size);
        Slice<Post> posts = switch (normalizedSort) {
            case "latest" -> postRepository.searchLatest(lastPostId, cursorCreatedAt(lastPostId), normalizedKeyword, pageable);
            case "popular" -> postRepository.searchPopular(lastPostId, likeCount(lastPostId), normalizedKeyword, pageable);
            case "comments" -> postRepository.searchByCommentCount(lastPostId, commentCount(lastPostId), normalizedKeyword, pageable);
            default -> throw new IllegalArgumentException("sort must be latest, popular, or comments");
        };
        return slice(posts, currentUserProvider.optionalCurrentUserId().orElse(null));
    }

    public PostResponse getPost(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        return toResponse(post, currentUserProvider.optionalCurrentUserId().orElse(null));
    }

    public Slice<PostResponse> getMyBookmarks(int size) {
        validateSize(size);
        Long memberId = currentUserProvider.currentUserId();
        Slice<Post> posts = postBookmarkRepository.findPostsByMemberIdOrderByCreatedAtDesc(memberId, PageRequest.of(0, size));
        return slice(posts, memberId);
    }

    private Slice<PostResponse> slice(Slice<Post> posts, Long memberId) {
        return new SliceImpl<>(toResponses(posts.getContent(), memberId), posts.getPageable(), posts.hasNext());
    }

    private List<PostResponse> toResponses(List<Post> posts, Long memberId) {
        if (posts.isEmpty()) return List.of();
        List<Long> postIds = posts.stream().map(Post::getId).toList();
        Map<Long, Long> likes = counts(postLikeRepository.countByPostIds(postIds));
        Map<Long, Long> comments = counts(commentRepository.countByPostIds(postIds));
        Set<Long> liked = memberId == null ? Set.of() : Set.copyOf(postLikeRepository.findLikedPostIds(postIds, memberId));
        Set<Long> bookmarked = memberId == null ? Set.of() : Set.copyOf(postBookmarkRepository.findBookmarkedPostIds(postIds, memberId));
        return posts.stream().map(post -> PostResponse.from(post,
                likes.getOrDefault(post.getId(), 0L), comments.getOrDefault(post.getId(), 0L),
                liked.contains(post.getId()), bookmarked.contains(post.getId()))).toList();
    }

    private PostResponse toResponse(Post post, Long memberId) {
        long likes = postLikeRepository.countByPostId(post.getId());
        long comments = commentRepository.countByPostId(post.getId());
        boolean liked = memberId != null && postLikeRepository.existsByPostIdAndMemberId(post.getId(), memberId);
        boolean bookmarked = memberId != null && postBookmarkRepository.existsByPostIdAndMemberId(post.getId(), memberId);
        return PostResponse.from(post, likes, comments, liked, bookmarked);
    }

    private Map<Long, Long> counts(List<Object[]> rows) {
        return rows.stream().collect(Collectors.toMap(row -> (Long) row[0], row -> ((Number) row[1]).longValue()));
    }

    private LocalDateTime cursorCreatedAt(Long postId) {
        return postId == null ? null : postRepository.findById(postId).map(Post::getCreatedAt).orElse(null);
    }

    private long likeCount(Long postId) { return postId == null ? 0L : postLikeRepository.countByPostId(postId); }
    private long commentCount(Long postId) { return postId == null ? 0L : commentRepository.countByPostId(postId); }
    private void validateSize(int size) { if (size < 1 || size > 100) throw new IllegalArgumentException("size must be between 1 and 100"); }
}
