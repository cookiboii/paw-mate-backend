package com.kindtail.adoptmate.adoption.controller;

import com.kindtail.adoptmate.adoption.domain.Adoption;
import com.kindtail.adoptmate.adoption.dto.AdoptionRequestDto;
import com.kindtail.adoptmate.adoption.dto.AdoptionResponseDto;
import com.kindtail.adoptmate.adoption.service.AdoptionService;
import com.kindtail.adoptmate.common.dto.CommonResDto;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.service.MemberService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/adoptions")
public class AdoptionController {

   private final AdoptionService adoptionService;
     private final MemberService memberService;
    public AdoptionController(AdoptionService adoptionService, MemberService memberService) {
        this.adoptionService = adoptionService;
        this.memberService = memberService;
    }
    @PostMapping("/animals/{animalId}/")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CommonResDto> registerAdoption(@RequestBody AdoptionRequestDto adoptionRequestDto) {
     Adoption adoption =adoptionService.applyAdoption(adoptionRequestDto);

        CommonResDto response = new CommonResDto(
                HttpStatus.ACCEPTED,
                "입양 신청이 완료되었습니다.",
                adoption.getId() // 필요 시 ID 반환
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }







}
