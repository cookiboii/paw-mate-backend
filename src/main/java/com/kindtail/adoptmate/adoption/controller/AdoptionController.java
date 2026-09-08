package com.kindtail.adoptmate.adoption.controller;

import com.kindtail.adoptmate.adoption.dto.AdoptionCreateRequest;
import com.kindtail.adoptmate.adoption.dto.AdoptionResponseDto;
import com.kindtail.adoptmate.adoption.dto.AdoptionUpdateRequestDto;
import com.kindtail.adoptmate.adoption.facade.AdoptionFacade;
import com.kindtail.adoptmate.adoption.service.AdoptionService;
import com.kindtail.adoptmate.auth.CustomUserDetails;
import com.kindtail.adoptmate.common.dto.CommonResDto;
import com.kindtail.adoptmate.common.dto.SuccessCode;
import com.kindtail.adoptmate.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/adoptions")
@RequiredArgsConstructor
public class AdoptionController implements AdoptionControllerDocs {

    private final AdoptionFacade adoptionFacade;
    private final AdoptionService adoptionService;
    private final MemberService memberService;

    @Override
    @PostMapping("/animals/{animalId}")
    public ResponseEntity<CommonResDto<AdoptionResponseDto>> registerAdoption(
            @PathVariable("animalId") Long animalId,
            @Valid @RequestBody AdoptionCreateRequest adoptionCreateRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long memberId = (userDetails.getId() != null) ? userDetails.getId() : memberService.getMemberIdByEmail(userDetails.getEmail());
        AdoptionResponseDto adoptionResponse = adoptionFacade.applyAdoption(adoptionCreateRequest, memberId, animalId);

        return CommonResDto.toResponseEntity(SuccessCode.ADOPTION_APPLY_SUCCESS, adoptionResponse);
    }

    @Override
    @GetMapping("/myAdoption")
    public ResponseEntity<CommonResDto<List<AdoptionResponseDto>>> myAdoption(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = (userDetails.getId() != null) ? userDetails.getId() : memberService.getMemberIdByEmail(userDetails.getEmail());
        List<AdoptionResponseDto> adoptions = adoptionService.getAdoptions(memberId);

        return CommonResDto.toResponseEntity(SuccessCode.ADOPTION_MY_LIST_SUCCESS, adoptions);
    }

    @Override
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResDto<List<AdoptionResponseDto>>> allAdoptions() {
        List<AdoptionResponseDto> adoptions = adoptionService.getAllAdoptions();
        return CommonResDto.toResponseEntity(SuccessCode.ADOPTION_ALL_SUCCESS, adoptions);
    }

    @Override
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResDto<Page<AdoptionResponseDto>>> getAdoptionList(Pageable pageable) {
        Page<AdoptionResponseDto> adoptions = adoptionService.getAllAdoptions(pageable);
        return CommonResDto.toResponseEntity(SuccessCode.ADOPTION_PAGE_SUCCESS, adoptions);
    }

    @Override
    @PutMapping("/{adoptionId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResDto<AdoptionResponseDto>> updateStatus(
            @PathVariable Long adoptionId,
            @Valid @RequestBody AdoptionUpdateRequestDto requestDto
    ) {
        AdoptionResponseDto adoptionResponse = adoptionFacade.updateStatus(adoptionId, requestDto.adoptionStatus());

        return CommonResDto.toResponseEntity(SuccessCode.ADOPTION_STATUS_UPDATE_SUCCESS, adoptionResponse);
    }
}
