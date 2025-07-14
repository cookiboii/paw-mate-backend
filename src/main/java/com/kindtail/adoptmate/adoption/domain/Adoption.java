package com.kindtail.adoptmate.adoption.domain;

import com.kindtail.adoptmate.animal.domain.Animal;
import com.kindtail.adoptmate.animal.domain.Status;
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
@Table(name = "tbl_adoption")
public class Adoption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column( name = "adoption_id")
    private Long id;


    private LocalDateTime apply_date;

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
        adoption.apply_date = LocalDateTime.now();
        return adoption;
    }
}
