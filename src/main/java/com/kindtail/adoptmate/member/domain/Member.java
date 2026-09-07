package com.kindtail.adoptmate.member.domain;

import com.kindtail.adoptmate.adoption.domain.Adoption;
import com.kindtail.adoptmate.animal.domain.Animal;
import com.kindtail.adoptmate.comment.domain.Comment;
import com.kindtail.adoptmate.common.domain.BaseTimeEntity;
import com.kindtail.adoptmate.post.domain.Post;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "member")
@SQLDelete(sql = "UPDATE member SET is_deleted = true, email = CONCAT('deleted_', member_id, '_', email) WHERE member_id = ?")
@SQLRestriction("is_deleted = false")
@Builder
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.USER;

    @Column
    private String socialId;

    @Column
    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false)
    @Builder.Default
    private AuthProvider authProvider = AuthProvider.EMAIL;

    @Builder.Default
    @OneToMany(mappedBy = "member")
    private List<Animal> animals = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "member")
    private List<Post> posts = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "member")
    private List<Adoption> adoptions = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "member")
    private List<Comment> comments = new ArrayList<>();

    public Member(String email, String password, String name, Role role) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
        this.authProvider = AuthProvider.EMAIL;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public void updateSocialInfo(AuthProvider authProvider, String socialId, String profileImage) {
        this.authProvider = authProvider != null ? authProvider : AuthProvider.EMAIL;
        this.socialId = socialId;
        if (profileImage != null && !profileImage.isBlank()) {
            this.profileImage = profileImage;
        }
    }

    public String getSocialProvider() {
        return this.authProvider != null ? this.authProvider.name() : null;
    }
}
