package com.kindtail.adoptmate.adoption.controller;

import com.kindtail.adoptmate.adoption.dto.AdoptionRequestDto;
import com.kindtail.adoptmate.adoption.dto.AdoptionResponseDto;
import com.kindtail.adoptmate.adoption.dto.AdoptionUpdateRequestDto;
import com.kindtail.adoptmate.adoption.service.AdoptionService;
import com.kindtail.adoptmate.auth.TokenUserInfo;
import com.kindtail.adoptmate.common.dto.CommonResDto;
import com.kindtail.adoptmate.member.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/adoptions")
public class AdoptionController {

    private final AdoptionService adoptionService;
    private final MemberService memberService;

    public AdoptionController(AdoptionService adoptionService, MemberService memberService) {
        this.adoptionService = adoptionService;
        this.memberService = memberService;
    }

    /** 입양 신청 */
    @PostMapping("/animals/{animalId}")
    public ResponseEntity<CommonResDto> registerAdoption(
            @PathVariable Long animalId,
            @RequestBody AdoptionRequestDto adoptionRequestDto
    ) {
        TokenUserInfo userInfo = (TokenUserInfo) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        Long memberId = memberService.getMemberIdByEmail(userInfo.getEmail());

        AdoptionResponseDto adoptionResponse = adoptionService.applyAdoption(adoptionRequestDto, memberId, animalId);

        CommonResDto response = new CommonResDto(
                HttpStatus.ACCEPTED,
                "입양 신청이 완료되었습니다.",
                adoptionResponse
        );

        return ResponseEntity.ok(response);
    }

    /** 내 입양 내역 */
    @GetMapping("/myAdoption")
    public ResponseEntity<CommonResDto> myAdoption() {
        TokenUserInfo userInfo = (TokenUserInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long memberId = memberService.getMemberIdByEmail(userInfo.getEmail());

        List<AdoptionResponseDto> adoptions = adoptionService.getAdoptions(memberId);

        CommonResDto response = new CommonResDto(
                HttpStatus.OK,
                "내 입양 내역 조회 성공",
                adoptions
        );

        return ResponseEntity.ok(response);
    }

    /** 전체 입양 내역 */
    @GetMapping("/all")
    public ResponseEntity<CommonResDto> allAdoptions() {
        List<AdoptionResponseDto> adoptions = adoptionService.getAllAdoptions();
        CommonResDto response = new CommonResDto(HttpStatus.OK, "전체조회", adoptions);
        return ResponseEntity.ok(response);
    }

    /** 입양 상태 변경 */
    @PutMapping("/{adoptionId}/status")
    public ResponseEntity<CommonResDto> updateStatus(
            @PathVariable Long adoptionId,
            @RequestBody AdoptionUpdateRequestDto requestDto
    ) {
        AdoptionResponseDto adoptionResponse = adoptionService.updateStatus(adoptionId, requestDto.adoptionStatus());

        return ResponseEntity.ok(
                new CommonResDto(HttpStatus.OK, "상태변경완료", adoptionResponse)
        );
    }
}
