package com.kindtail.adoptmate.member.domain;

import com.kindtail.adoptmate.animal.domain.Animal;
import com.kindtail.adoptmate.post.domain.Post;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "tbl_member")
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name="email" ,unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;



    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.USER;





    public Member(String email, String password, String name, Role role) {
        this.email = email;
        this.password = password;
        this.name = name;

        this.role = role;
    }

    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Animal> animals = new ArrayList<>();

     public void updatePassword(String password) {
         this.password = password;
     }


    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE, orphanRemoval = true)
    //  부모가 삭제되면 자식이 삭제된다 즉 멤버가 회원탈퇴면 글삭제된다
    private List<Post> posts = new ArrayList<>();

}
