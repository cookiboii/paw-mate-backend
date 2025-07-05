package com.kindtail.adoptmate.animal.controller;

import com.kindtail.adoptmate.animal.domain.Animal;
import com.kindtail.adoptmate.animal.dto.AnimalCreateRequest;
import com.kindtail.adoptmate.animal.dto.AnimalResponse;
import com.kindtail.adoptmate.animal.service.AnimalService;
import com.kindtail.adoptmate.common.dto.CommonResDto;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.service.MemberService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/animals")

public class AnimalController {

    private final AnimalService animalService;


    public AnimalController(AnimalService animalService) {
        this.animalService = animalService;
       
    }

    @PostMapping("/register")

    public ResponseEntity<CommonResDto> adoptAnimal(@RequestBody AnimalCreateRequest animalCreateRequest) {

        Animal animal = animalService.registerAnimal(animalCreateRequest);

        CommonResDto response = new CommonResDto(
                HttpStatus.CREATED,
                "등록 성공",
                animal
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }



    @GetMapping("/list")
    public ResponseEntity<Page<AnimalResponse>> getAnimalList(@RequestParam int page, @RequestParam int size) {
        Page<AnimalResponse> animalList = animalService.getAllAnimals(PageRequest.of(page, size));
        return ResponseEntity.status(HttpStatus.OK).body(animalList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResDto> getAnimalById(@PathVariable Long id) {
        AnimalResponse animal = animalService.getAnimal(id);
        return ResponseEntity.ok(
                new CommonResDto(HttpStatus.OK, "상세 조회 성공", animal)
        );
    }

}