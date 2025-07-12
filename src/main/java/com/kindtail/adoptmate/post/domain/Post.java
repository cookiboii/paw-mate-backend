package com.kindtail.adoptmate.post.domain;


import com.kindtail.adoptmate.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor
@Table(name = "tbl_post")
@Builder
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private   Long id;
    private String title;
    @Lob
    private String content;
    @Lob
    private String image;

  private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private  Member member;

   public void updatePost(String title, String content, String image) {
       this.title = title;
       this.content = content;
       this.image = image;
   }

}
