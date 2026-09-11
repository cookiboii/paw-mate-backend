package com.kindtail.adoptmate.animal.service;

import com.kindtail.adoptmate.animal.domain.Animal;
import com.kindtail.adoptmate.animal.domain.AnimalFavorite;
import com.kindtail.adoptmate.animal.dto.AnimalResponse;
import com.kindtail.adoptmate.animal.dto.FavoriteToggleResponse;
import com.kindtail.adoptmate.animal.repository.AnimalFavoriteRepository;
import com.kindtail.adoptmate.animal.repository.AnimalRepository;
import com.kindtail.adoptmate.common.exception.CustomException;
import com.kindtail.adoptmate.common.exception.ErrorCode;
import com.kindtail.adoptmate.common.lock.DistributedLockTemplate;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnimalFavoriteService {

    private final AnimalFavoriteRepository animalFavoriteRepository;
    private final AnimalRepository animalRepository;
    private final MemberRepository memberRepository;
    private final DistributedLockTemplate distributedLockTemplate;
    private final TransactionTemplate transactionTemplate;

    // 클래스의 읽기 전용 트랜잭션을 중단하고, 분산 락을 획득한 뒤 내부 TransactionTemplate에서
    // 쓰기 트랜잭션을 시작한다. 락보다 트랜잭션이 먼저 시작되면 읽기 전용 연결을 재사용할 수 있다.
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public FavoriteToggleResponse toggleFavorite(Long animalId, Long memberId) {
        return distributedLockTemplate.execute(
                "favorite:" + memberId + ":" + animalId,
                () -> transactionTemplate.execute(status -> toggleFavoriteWithinLock(animalId, memberId))
        );
    }

    private FavoriteToggleResponse toggleFavoriteWithinLock(Long animalId, Long memberId) {
        Animal animal = animalRepository.findById(animalId)
                .orElseThrow(() -> new CustomException(ErrorCode.ANIMAL_NOT_FOUND));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Optional<AnimalFavorite> existingFavorite = animalFavoriteRepository.findByMemberAndAnimal(member, animal);

        boolean isFavorite;
        if (existingFavorite.isPresent()) {
            animalFavoriteRepository.delete(existingFavorite.get());
            isFavorite = false;
            log.info("Member {} unfavorited Animal {}", memberId, animalId);
        } else {
            AnimalFavorite favorite = AnimalFavorite.builder()
                    .member(member)
                    .animal(animal)
                    .build();
            animalFavoriteRepository.save(favorite);
            isFavorite = true;
            log.info("Member {} favorited Animal {}", memberId, animalId);
        }

        long favoriteCount = animalFavoriteRepository.countByAnimalId(animalId);
        return new FavoriteToggleResponse(animalId, isFavorite, favoriteCount);
    }

    @Transactional
    public FavoriteToggleResponse removeFavorite(Long animalId, Long memberId) {
        Animal animal = animalRepository.findById(animalId)
                .orElseThrow(() -> new CustomException(ErrorCode.ANIMAL_NOT_FOUND));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        animalFavoriteRepository.findByMemberAndAnimal(member, animal)
                .ifPresent(favorite -> {
                    animalFavoriteRepository.delete(favorite);
                    log.info("Member {} explicitly removed favorite on Animal {}", memberId, animalId);
                });

        long favoriteCount = animalFavoriteRepository.countByAnimalId(animalId);
        return new FavoriteToggleResponse(animalId, false, favoriteCount);
    }

    public Page<AnimalResponse> getMyFavoriteAnimals(Long memberId, Pageable pageable) {
        if (!memberRepository.existsById(memberId)) {
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        }

        Page<Animal> animals = animalFavoriteRepository.findFavoriteAnimalsByMemberId(memberId, pageable);
        return animals.map(AnimalResponse::from);
    }

    public boolean isFavorite(Long animalId, Long memberId) {
        if (memberId == null) {
            return false;
        }
        return animalFavoriteRepository.existsByMemberIdAndAnimalId(memberId, animalId);
    }

    public long getFavoriteCount(Long animalId) {
        return animalFavoriteRepository.countByAnimalId(animalId);
    }
}
