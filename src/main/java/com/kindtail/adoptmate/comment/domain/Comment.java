package com.kindtail.adoptmate.comment.domain;


import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.post.domain.Post;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "tbl_comment")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @Lob
   private String content;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;
    private LocalDateTime creationDate;
    @Builder.Default
    @OneToMany(mappedBy = "parent",  cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Comment> children = new ArrayList<>();


    public void updateComment(String content ) {
   this.content = content;

    }
}
