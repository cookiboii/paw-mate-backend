package com.kindtail.adoptmate.adoption.domain;

import com.kindtail.adoptmate.animal.domain.Animal;

import com.kindtail.adoptmate.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
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
public class Adoption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column( name = "adoption_id")
    private Long id;


    @Column(name = "apply_date")
    private LocalDateTime applyDate;

    @Lob
    private String interview;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdoptionStatus status;

    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(name = "animal_id")
   private Animal animal;



    public void updateAdoption(AdoptionStatus status) {
        this.status = status;
    }
    public static Adoption of(Member member, Animal animal, String interview, AdoptionStatus status) {
        Adoption adoption = new Adoption();
        adoption.member = member;
        adoption.animal = animal;
        adoption.status = status;
        adoption.interview = interview;
        adoption.applyDate = LocalDateTime.now();
        return adoption;
    }
}
