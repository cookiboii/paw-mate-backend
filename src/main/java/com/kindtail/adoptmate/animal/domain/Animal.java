package com.kindtail.adoptmate.animal.domain;

import com.kindtail.adoptmate.animal.dto.AnimalCreateRequest;
import com.kindtail.adoptmate.animal.dto.AnimalStatusUpdateRequest;
import com.kindtail.adoptmate.member.domain.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tbl_animal")
public class Animal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "animal_id")
    private Long id;

    private String species;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String breed;

    private String color;

    @Enumerated(EnumType.STRING)
    private Status status=Status.PROTECTED;

    private Long age;

    @Lob
    private String image;

    @ManyToOne(fetch = FetchType.LAZY  )
    @JoinColumn(name = "member_id", nullable = false) // 외래 키 컬럼명 지정
    private Member member;


    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;


    public   void   updatestatus (AnimalStatusUpdateRequest request) {

       this.status= request.status();
    }

}
