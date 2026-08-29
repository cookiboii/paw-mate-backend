package com.kindtail.adoptmate.adoption.domain;

import com.kindtail.adoptmate.animal.domain.Animal;
import com.kindtail.adoptmate.common.domain.BaseTimeEntity;
import com.kindtail.adoptmate.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(
        name = "adoption",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_adoption_member_animal",
                        columnNames = {"member_id", "animal_id"}
                )
        }
)
@SQLDelete(sql = "UPDATE adoption SET is_deleted = true WHERE adoption_id = ? AND version = ?")
@SQLRestriction("is_deleted = false")
public class Adoption extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "adoption_id")
    private Long id;

    @CreationTimestamp
    @Column(name = "apply_date")
    private LocalDateTime applyDate;

    @Lob
    private String interview;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdoptionStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "animal_id")
    private Animal animal;

    @Column(nullable = false, length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private HousingType housingType;

    @Column(nullable = false, length = 50)
    private String hasPet;           // 현재 반려동물 유무 (없음, 개 1마리 등)

    @Column(columnDefinition = "TEXT", nullable = false)
    private String reason;           // 입양 동기 및 돌봄 계획 (각오)

    @Version
    @Builder.Default
    @Column(columnDefinition = "BIGINT DEFAULT 0")
    private Long version = 0L;

    public static Adoption of(
            Member member,
            Animal animal,
            String phone,
            HousingType housingType,
            String hasPet,
            String reason,
            AdoptionStatus status
    ) {
        return Adoption.builder()
                .member(member)
                .animal(animal)
                .phone(phone)
                .housingType(housingType)
                .hasPet(hasPet)
                .reason(reason)
                .status(status)
                .applyDate(LocalDateTime.now())
                .build();
    }

    public static Adoption of(Member member, Animal animal, String interview, AdoptionStatus status) {
        return Adoption.builder()
                .member(member)
                .animal(animal)
                .interview(interview)
                .phone("010-0000-0000")
                .housingType(HousingType.APARTMENT)
                .hasPet("없음")
                .reason(interview != null ? interview : "입양 신청")
                .status(status)
                .applyDate(LocalDateTime.now())
                .build();
    }

    public void updateAdoption(AdoptionStatus status) {
        this.status = status;
    }

}
