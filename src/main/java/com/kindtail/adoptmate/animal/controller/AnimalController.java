package com.kindtail.adoptmate.animal.controller;

import com.kindtail.adoptmate.animal.domain.Animal;
import com.kindtail.adoptmate.animal.domain.Species;
import com.kindtail.adoptmate.animal.dto.AnimalCreateRequest;
import com.kindtail.adoptmate.animal.dto.AnimalResponse;
import com.kindtail.adoptmate.animal.dto.AnimalStatusUpdateRequest;
import com.kindtail.adoptmate.animal.service.AnimalService;
import com.kindtail.adoptmate.common.dto.CommonResDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/animals")
@RequiredArgsConstructor
public class AnimalController {

    private final AnimalService animalService;

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResDto> adoptAnimal(@RequestBody AnimalCreateRequest animalCreateRequest) {
        Animal animal = animalService.registerAnimal(animalCreateRequest);
        AnimalResponse responseDto = AnimalResponse.from(animal);

        CommonResDto response = new CommonResDto(
                HttpStatus.CREATED,
                "등록 성공",
                responseDto
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/list")
    public ResponseEntity<Page<AnimalResponse>> getAnimalList(@RequestParam int page, @RequestParam int size) {
        Page<AnimalResponse> animalList = animalService.getAllAnimals(PageRequest.of(page, size));
        return ResponseEntity.status(HttpStatus.OK).body(animalList);
    }

    @GetMapping("/species")
    public ResponseEntity<Page<AnimalResponse>> getAnimalsBySpecies(
            @RequestParam Species species,
            @RequestParam int page,
            @RequestParam int size
    ) {
        Page<AnimalResponse> animalList = animalService.getAnimalsBySpecies(species, PageRequest.of(page, size));
        return ResponseEntity.status(HttpStatus.OK).body(animalList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResDto> getAnimalById(@PathVariable Long id) {
        AnimalResponse animal = animalService.getAnimal(id);
        return ResponseEntity.ok(
                new CommonResDto(HttpStatus.OK, "상세 조회 성공", animal)
        );
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResDto> updateAnimal(
            @PathVariable Long id,
            @RequestBody AnimalStatusUpdateRequest request
    ) {
        AnimalResponse animal = animalService.updateAnimal(id, request);
        CommonResDto response = new CommonResDto(HttpStatus.OK, "상태가 성공적으로 변경되었습니다.", animal);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResDto> deleteAnimal(@PathVariable Long id) {
        animalService.deleteAnimal(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}