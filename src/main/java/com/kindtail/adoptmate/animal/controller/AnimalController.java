package com.kindtail.adoptmate.animal.controller;

import com.kindtail.adoptmate.animal.domain.Species;
import com.kindtail.adoptmate.animal.dto.AnimalCreateRequest;
import com.kindtail.adoptmate.animal.dto.AnimalResponse;
import com.kindtail.adoptmate.animal.dto.AnimalStatusUpdateRequest;
import com.kindtail.adoptmate.animal.dto.FavoriteToggleResponseDto;
import com.kindtail.adoptmate.animal.service.AnimalFavoriteService;
import com.kindtail.adoptmate.animal.service.AnimalService;
import com.kindtail.adoptmate.auth.CustomUserDetails;
import com.kindtail.adoptmate.auth.SecurityUtil;
import com.kindtail.adoptmate.common.dto.CommonResDto;
import com.kindtail.adoptmate.common.dto.SuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping({"/api/v1/animals", "/animals"})
@RequiredArgsConstructor
@Validated
public class AnimalController implements AnimalControllerDocs {

    private final AnimalService animalService;
    private final AnimalFavoriteService animalFavoriteService;

    @Override
    @PostMapping({"", "/register"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResDto<AnimalResponse>> adoptAnimal(@Valid @RequestBody AnimalCreateRequest animalCreateRequest) {
        AnimalResponse responseDto = animalService.registerAnimal(animalCreateRequest);
        return CommonResDto.toResponseEntity(SuccessCode.ANIMAL_REGISTER_SUCCESS, responseDto);
    }

    @Override
    @GetMapping({"", "/list"})
    public ResponseEntity<CommonResDto<Page<AnimalResponse>>> getAnimalList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<AnimalResponse> animalList = animalService.getAllAnimals(PageRequest.of(page, size));
        return CommonResDto.toResponseEntity(SuccessCode.ANIMAL_LIST_SUCCESS, animalList);
    }

    @Override
    @GetMapping("/cursor")
    public ResponseEntity<CommonResDto<Slice<AnimalResponse>>> getAnimalsByCursor(
            @RequestParam(required = false) Long lastAnimalId,
            @RequestParam(defaultValue = "10") int size
    ) {
        Slice<AnimalResponse> animalSlice = animalService.getAnimalsByCursor(lastAnimalId, size);
        return CommonResDto.toResponseEntity(SuccessCode.ANIMAL_LIST_SUCCESS, animalSlice);
    }

    @Override
    @GetMapping("/species")
    public ResponseEntity<CommonResDto<Page<AnimalResponse>>> getAnimalsBySpecies(
            @RequestParam Species species,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<AnimalResponse> animalList = animalService.getAnimalsBySpecies(species, PageRequest.of(page, size));
        return CommonResDto.toResponseEntity(SuccessCode.ANIMAL_SPECIES_LIST_SUCCESS, animalList);
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<CommonResDto<AnimalResponse>> getAnimalById(@PathVariable Long id) {
        AnimalResponse animal = animalService.getAnimal(id);
        return CommonResDto.toResponseEntity(SuccessCode.ANIMAL_DETAIL_SUCCESS, animal);
    }

    @Override
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResDto<AnimalResponse>> updateAnimal(
            @PathVariable Long id,
            @Valid @RequestBody AnimalStatusUpdateRequest request
    ) {
        AnimalResponse animal = animalService.updateAnimal(id, request);
        return CommonResDto.toResponseEntity(SuccessCode.ANIMAL_STATUS_UPDATE_SUCCESS, animal);
    }

    @Override
    @DeleteMapping(value = {"/{id}", "/delete/{id}"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResDto<Void>> deleteAnimal(@PathVariable Long id) {
        animalService.deleteAnimal(id);
        return CommonResDto.toResponseEntity(SuccessCode.ANIMAL_DELETE_SUCCESS);
    }

    @Override
    @PostMapping("/{id}/favorite")
    public ResponseEntity<CommonResDto<FavoriteToggleResponseDto>> toggleFavorite(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long memberId = userDetails != null ? userDetails.getId() : SecurityUtil.getCurrentUserId();
        FavoriteToggleResponseDto response = animalFavoriteService.toggleFavorite(id, memberId);
        return CommonResDto.toResponseEntity(SuccessCode.ANIMAL_FAVORITE_TOGGLE_SUCCESS, response);
    }

    @Override
    @GetMapping("/favorites/my")
    public ResponseEntity<CommonResDto<Page<AnimalResponse>>> getMyFavoriteAnimals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long memberId = userDetails != null ? userDetails.getId() : SecurityUtil.getCurrentUserId();
        Page<AnimalResponse> response = animalFavoriteService.getMyFavoriteAnimals(memberId, PageRequest.of(page, size));
        return CommonResDto.toResponseEntity(SuccessCode.ANIMAL_FAVORITE_LIST_SUCCESS, response);
    }

    @Override
    @DeleteMapping("/{id}/favorite")
    public ResponseEntity<CommonResDto<FavoriteToggleResponseDto>> deleteFavorite(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long memberId = userDetails != null ? userDetails.getId() : SecurityUtil.getCurrentUserId();
        FavoriteToggleResponseDto response = animalFavoriteService.removeFavorite(id, memberId);
        return CommonResDto.toResponseEntity(SuccessCode.ANIMAL_FAVORITE_DELETE_SUCCESS, response);
    }
}
