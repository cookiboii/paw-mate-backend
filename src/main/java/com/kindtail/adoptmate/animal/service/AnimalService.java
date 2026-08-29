package com.kindtail.adoptmate.animal.service;

import com.kindtail.adoptmate.animal.domain.Animal;
import com.kindtail.adoptmate.animal.domain.Species;
import com.kindtail.adoptmate.animal.dto.AnimalCreateRequest;
import com.kindtail.adoptmate.animal.dto.AnimalResponse;
import com.kindtail.adoptmate.animal.dto.AnimalStatusUpdateRequest;
import com.kindtail.adoptmate.animal.repository.AnimalRepository;

import com.kindtail.adoptmate.common.exception.CustomException;
import com.kindtail.adoptmate.common.exception.ErrorCode;
import com.kindtail.adoptmate.auth.TokenUserInfo;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnimalService {

    private final AnimalRepository animalRepository;
    private final MemberRepository memberRepository;

    public AnimalService(AnimalRepository animalRepository, MemberRepository memberRepository) {
        this.animalRepository = animalRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public Animal registerAnimal(AnimalCreateRequest animalCreateRequest) {
        // 현재 인증된 사용자 정보 꺼내기
        TokenUserInfo userInfo = (TokenUserInfo) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        // 사용자 이메일로 Member 조회
        Member member = memberRepository.findByEmail(userInfo.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 동물 엔티티 생성
        Animal animal = Animal.builder()
                .species(animalCreateRequest.species())
                .age(animalCreateRequest.age())
                .breed(animalCreateRequest.breed())
                .color(animalCreateRequest.color())
                .status(animalCreateRequest.status())
                .gender(animalCreateRequest.gender())
                .image(animalCreateRequest.image())
                .member(member)
                .build();

        return animalRepository.save(animal);
    }

    @Transactional(readOnly = true)
    public Page<AnimalResponse> getAllAnimals(Pageable pageable) {
        Page<Animal> animals = animalRepository.findAll(pageable);
        return animals.map(AnimalResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<AnimalResponse> getAnimalsBySpecies(Species species, Pageable pageable) {
        Page<Animal> animals = animalRepository.findBySpecies(species, pageable);
        return animals.map(AnimalResponse::from);
    }
    @Transactional(readOnly = true)
    public AnimalResponse getAnimal(Long id) {
        Animal animal = animalRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ANIMAL_NOT_FOUND));

        return AnimalResponse.from(animal);
    }

    @Transactional
    public AnimalResponse updateAnimal(Long id, AnimalStatusUpdateRequest request) {
        Animal animal = animalRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ANIMAL_NOT_FOUND));
        animal.updateStatus(request);
        return AnimalResponse.from(animal);
    }

    @Transactional
    public void deleteAnimal(Long id) {
        animalRepository.deleteById(id);
    }

}
