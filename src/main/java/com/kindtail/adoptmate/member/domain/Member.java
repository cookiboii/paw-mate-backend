package com.kindtail.adoptmate.member.domain;

import com.kindtail.adoptmate.animal.domain.Animal;
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

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Animal> animals = new ArrayList<>();
}
