package com.kindtail.adoptmate.post.domain;

import com.kindtail.adoptmate.auth.TokenUserInfo;
import com.kindtail.adoptmate.common.domain.BaseTimeEntity;
import com.kindtail.adoptmate.common.exception.CustomException;
import com.kindtail.adoptmate.common.exception.ErrorCode;
import com.kindtail.adoptmate.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor
@Table(name = "post")
@SQLDelete(sql = "UPDATE post SET is_deleted = true WHERE post_id = ? AND version = ?")
@SQLRestriction("is_deleted = false")
@Builder
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    private String title;

    @Lob
    private String content;

    @Lob
    private String image;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Version
    @Builder.Default
    @Column(columnDefinition = "BIGINT DEFAULT 0")
    private Long version = 0L;

    public void updatePost(String title, String content, String image) {
        this.title = title;
        this.content = content;
        this.image = image;
    }

    /**
     * 작성자 본인 또는 관리자 권한 검증 (Tell, Don't Ask)
     */
    public void validateAuthorOrAdmin(TokenUserInfo userInfo) {
        if (userInfo == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        boolean isAuthor = this.member != null && this.member.getEmail().equals(userInfo.getEmail());
        boolean isAdmin = userInfo.isAdmin();

        if (!isAuthor && !isAdmin) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_AUTHOR);
        }
    }
}
