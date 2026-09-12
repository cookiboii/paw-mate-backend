package com.kindtail.adoptmate.post.domain;

import com.kindtail.adoptmate.common.domain.BaseTimeEntity;
import com.kindtail.adoptmate.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "post_like", uniqueConstraints = @UniqueConstraint(name = "uk_post_like_post_member", columnNames = {"post_id", "member_id"}))
public class PostLike extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_like_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Builder
    public PostLike(Post post, Member member) { this.post = post; this.member = member; }
}
