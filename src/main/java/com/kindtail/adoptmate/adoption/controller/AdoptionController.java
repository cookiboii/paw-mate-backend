package com.kindtail.adoptmate.adoption.controller;

import com.kindtail.adoptmate.adoption.domain.Adoption;
import com.kindtail.adoptmate.adoption.domain.AdoptionStatus;
import com.kindtail.adoptmate.adoption.dto.AdoptionRequestDto;
import com.kindtail.adoptmate.adoption.dto.AdoptionResponseDto;
import com.kindtail.adoptmate.adoption.dto.AdoptionUpdateRequestDto;
import com.kindtail.adoptmate.adoption.service.AdoptionService;
import com.kindtail.adoptmate.auth.TokenUserInfo;
import com.kindtail.adoptmate.common.dto.CommonResDto;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.service.MemberService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
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
    public ResponseEntity<CommonResDto> registerAdoption(
            @PathVariable Long animalId,
            @RequestBody AdoptionRequestDto adoptionRequestDto
    ) {

        TokenUserInfo userInfo = (TokenUserInfo) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        Long memberId = memberService.getMemberIdByEmail(userInfo.getEmail());


        Adoption adoption = adoptionService.applyAdoption(adoptionRequestDto, memberId, animalId);


        CommonResDto response = new CommonResDto(
                HttpStatus.ACCEPTED,
                "입양 신청이 완료되었습니다.",
                adoption.getStatus()
        );

        return ResponseEntity.ok(response);
    }
    @GetMapping("/myAdoption")
    public ResponseEntity<CommonResDto> myAdoption() {
        TokenUserInfo userInfo = (TokenUserInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long memberId = memberService.getMemberIdByEmail(userInfo.getEmail());
        List <AdoptionResponseDto> adoptions = adoptionService.getAdoptions(memberId);
        CommonResDto response = new CommonResDto(  HttpStatus.OK,
                "내 입양 내역 조회 성공",
                adoptions);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<CommonResDto> allAdoptions() {
    List <AdoptionResponseDto> adoptions =adoptionService.getAllAdoptions();
    CommonResDto response = new CommonResDto(HttpStatus.OK, "전체조회", adoptions);
    return ResponseEntity.ok(response);
    }
  @PutMapping("/{adoptionId}/status")
   public ResponseEntity<CommonResDto> updateStatus(   @PathVariable Long adoptionId,
                                                       @RequestBody AdoptionUpdateRequestDto requestDto) {

       Adoption adoption = adoptionService.updateStatus(adoptionId, requestDto.adoptionStatus());
       
       return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "상태변경완료", adoption));
   }
}
