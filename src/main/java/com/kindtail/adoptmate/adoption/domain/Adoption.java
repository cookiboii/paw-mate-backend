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
@Table(name = "tbl_aboption")
public class Adoption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column( name = "adoption_id")
    private Long id;


    private LocalDateTime apply_date;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdoptionStatus status;

    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "animal_id")
   private Animal animal;



    public void update(AdoptionStatus status) {
        this.status = status;
    }
    public static Adoption of(Member member, Animal animal) {
        Adoption adoption = new Adoption();
        adoption.member = member;
        adoption.animal = animal;
        adoption.status = AdoptionStatus.PENDING;
        adoption.apply_date = LocalDateTime.now();
        return adoption;
    }
}
