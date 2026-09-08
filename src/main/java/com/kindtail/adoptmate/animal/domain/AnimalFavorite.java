package com.kindtail.adoptmate.animal.domain;

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
@Table(
        name = "animal_favorite",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_animal_favorite_member_animal",
                        columnNames = {"member_id", "animal_id"}
                )
        }
)
public class AnimalFavorite extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "animal_favorite_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "animal_id", nullable = false)
    private Animal animal;

    @Builder
    public AnimalFavorite(Member member, Animal animal) {
        this.member = member;
        this.animal = animal;
    }
}
