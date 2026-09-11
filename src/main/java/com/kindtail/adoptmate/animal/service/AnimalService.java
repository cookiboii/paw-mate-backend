package com.kindtail.adoptmate.animal.service;

import com.kindtail.adoptmate.animal.domain.Animal;
import com.kindtail.adoptmate.animal.domain.Species;
import com.kindtail.adoptmate.animal.dto.AnimalCreateRequest;
import com.kindtail.adoptmate.animal.dto.AnimalResponse;
import com.kindtail.adoptmate.animal.dto.AnimalStatusUpdateRequest;
import com.kindtail.adoptmate.animal.repository.AnimalRepository;

import com.kindtail.adoptmate.common.exception.CustomException;
import com.kindtail.adoptmate.common.exception.ErrorCode;
import com.kindtail.adoptmate.auth.CustomUserDetails;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.repository.MemberRepository;
import com.kindtail.adoptmate.auth.SecurityUtil;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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
    public AnimalResponse createAnimal(AnimalCreateRequest request) {
        String email = SecurityUtil.getCurrentUserEmail();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 동물 엔티티 생성
        Animal animal = Animal.builder()
                .species(request.species())
                .age(request.age())
                .breed(request.breed())
                .color(request.color())
                .status(request.status())
                .gender(request.gender())
                .image(request.image())
                .member(member)
                .build();

        return AnimalResponse.from(animalRepository.save(animal));
    }

    @Transactional(readOnly = true)
    public Page<AnimalResponse> getAllAnimals(Pageable pageable) {
        Page<Animal> animals = animalRepository.findAll(pageable);
        return animals.map(AnimalResponse::from);
    }

    @Transactional(readOnly = true)
    public Slice<AnimalResponse> getAnimalsByCursor(Long lastAnimalId, int size) {
        Pageable pageable = PageRequest.of(0, size);
        Slice<Animal> animals = animalRepository.findAnimalsByCursor(lastAnimalId, pageable);
        return animals.map(AnimalResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<AnimalResponse> getAnimalsBySpecies(Species species, Pageable pageable) {
        Page<Animal> animals = animalRepository.findBySpecies(species, pageable);
        return animals.map(AnimalResponse::from);
    }

    @Transactional(readOnly = true)
    public Slice<AnimalResponse> getAnimalsBySpeciesAndCursor(Species species, Long lastAnimalId, int size) {
        Pageable pageable = PageRequest.of(0, size);
        Slice<Animal> animals = animalRepository.findAnimalsBySpeciesAndCursor(species, lastAnimalId, pageable);
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
        animal.updateStatus(request.status());
        return AnimalResponse.from(animal);
    }

    @Transactional
    public void deleteAnimal(Long id) {
        animalRepository.deleteById(id);
    }

}
