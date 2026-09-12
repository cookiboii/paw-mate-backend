package com.kindtail.adoptmate.comment.domain;

import com.kindtail.adoptmate.auth.CustomUserDetails;
import com.kindtail.adoptmate.common.domain.BaseTimeEntity;
import com.kindtail.adoptmate.common.exception.CustomException;
import com.kindtail.adoptmate.common.exception.ErrorCode;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.post.domain.Post;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "comment")
@SQLDelete(sql = "UPDATE comment SET is_deleted = true WHERE comment_id = ?")
@SQLRestriction("is_deleted = false")
public class Comment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @Lob
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @NotFound(action = NotFoundAction.IGNORE)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Builder.Default
    @BatchSize(size = 100)
    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Comment> children = new ArrayList<>();

    public void updateComment(String content) {
        this.content = content;
    }

    public LocalDateTime getCreationDate() {
        return getCreatedAt();
    }

    /**
     * 작성자 본인 또는 관리자 권한 검증 (Tell, Don't Ask)
     */
    public void validateAuthorOrAdmin(Long currentUserId, boolean isAdmin) {
        if (currentUserId == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        boolean isAuthor = this.member != null && this.member.getId() != null && this.member.getId().equals(currentUserId);
        if (!isAuthor && !isAdmin) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_AUTHOR);
        }
    }

    public void validateAuthorOrAdmin(CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        validateAuthorOrAdmin(userDetails.getId(), userDetails.isAdmin());
    }
}
