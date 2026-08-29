package com.kindtail.adoptmate.animal.domain;

import com.kindtail.adoptmate.adoption.domain.Adoption;
import com.kindtail.adoptmate.animal.dto.AnimalStatusUpdateRequest;
import com.kindtail.adoptmate.common.domain.BaseTimeEntity;
import com.kindtail.adoptmate.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "animal")
@SQLDelete(sql = "UPDATE animal SET is_deleted = true WHERE animal_id = ? AND version = ?")
@SQLRestriction("is_deleted = false")
public class Animal extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "animal_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    private Species species;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String breed;

    private String color;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.PROTECTED;

    private Long age;

    @Lob
    private String image;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Version
    @Builder.Default
    @Column(columnDefinition = "BIGINT DEFAULT 0")
    private Long version = 0L;

    public void updateStatus(AnimalStatusUpdateRequest request) {
        this.status = request.status();
    }

    @Builder.Default
    @BatchSize(size = 100)
    @OneToMany(mappedBy = "animal", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Adoption> adoptions = new ArrayList<>();
}
