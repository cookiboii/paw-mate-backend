package com.kindtail.adoptmate.adoption.controller;

import com.kindtail.adoptmate.adoption.dto.AdoptionCreateRequest;
import com.kindtail.adoptmate.adoption.dto.AdoptionResponseDto;
import com.kindtail.adoptmate.adoption.dto.AdoptionUpdateRequestDto;
import com.kindtail.adoptmate.adoption.facade.AdoptionFacade;
import com.kindtail.adoptmate.adoption.service.AdoptionService;
import com.kindtail.adoptmate.auth.TokenUserInfo;
import com.kindtail.adoptmate.common.dto.CommonResDto;
import com.kindtail.adoptmate.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/adoptions")
@RequiredArgsConstructor
public class AdoptionController {

    private final AdoptionFacade adoptionFacade;
    private final AdoptionService adoptionService;
    private final MemberService memberService;

    /** 입양 신청 */
    @PostMapping("/animals/{animalId}")
    public ResponseEntity<CommonResDto> registerAdoption(
            @PathVariable("animalId") Long animalId,
            @Valid @RequestBody AdoptionCreateRequest adoptionCreateRequest
    ) {
        TokenUserInfo userInfo = (TokenUserInfo) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        Long memberId = memberService.getMemberIdByEmail(userInfo.getEmail());

        AdoptionResponseDto adoptionResponse = adoptionFacade.applyAdoption(adoptionCreateRequest, memberId, animalId);

        CommonResDto response = new CommonResDto(
                HttpStatus.CREATED,
                "입양 신청이 완료되었습니다.",
                adoptionResponse
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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

    /** 전체 입양 내역 (관리자 전용) */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResDto> allAdoptions() {
        List<AdoptionResponseDto> adoptions = adoptionService.getAllAdoptions();
        CommonResDto response = new CommonResDto(HttpStatus.OK, "전체조회", adoptions);
        return ResponseEntity.ok(response);
    }

    /** 전체 입양 내역 페이징 (관리자 전용) */
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResDto> getAdoptionList(Pageable pageable) {
        Page<AdoptionResponseDto> adoptions = adoptionService.getAllAdoptions(pageable);
        return ResponseEntity.ok(new CommonResDto(HttpStatus.OK, "전체 입양 목록 조회 성공", adoptions));
    }

    /** 입양 상태 변경 (관리자 전용) */
    @PutMapping("/{adoptionId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResDto> updateStatus(
            @PathVariable Long adoptionId,
            @Valid @RequestBody AdoptionUpdateRequestDto requestDto
    ) {
        AdoptionResponseDto adoptionResponse = adoptionFacade.updateStatus(adoptionId, requestDto.adoptionStatus());

        return ResponseEntity.ok(
                new CommonResDto(HttpStatus.OK, "상태변경완료", adoptionResponse)
        );
    }
}
