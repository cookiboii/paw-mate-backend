package com.kindtail.adoptmate.adoption.controller;

import com.kindtail.adoptmate.adoption.dto.AdoptionCreateRequest;
import com.kindtail.adoptmate.adoption.dto.AdoptionResponse;
import com.kindtail.adoptmate.adoption.dto.AdoptionStatusUpdateRequest;
import com.kindtail.adoptmate.adoption.facade.AdoptionFacade;
import com.kindtail.adoptmate.adoption.service.AdoptionService;
import com.kindtail.adoptmate.auth.CustomUserDetails;
import com.kindtail.adoptmate.common.dto.CommonResponse;
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
    public ResponseEntity<CommonResponse<AdoptionResponse>> applyAdoption(
            @PathVariable("animalId") Long animalId,
            @Valid @RequestBody AdoptionCreateRequest adoptionCreateRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long memberId = (userDetails.getId() != null) ? userDetails.getId() : memberService.getMemberIdByEmail(userDetails.getEmail());
        AdoptionResponse adoptionResponse = adoptionFacade.applyAdoption(adoptionCreateRequest, memberId, animalId);

        return CommonResponse.toResponseEntity(SuccessCode.ADOPTION_APPLY_SUCCESS, adoptionResponse);
    }

    @Override
    @GetMapping("/myAdoption")
    public ResponseEntity<CommonResponse<List<AdoptionResponse>>> myAdoption(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = (userDetails.getId() != null) ? userDetails.getId() : memberService.getMemberIdByEmail(userDetails.getEmail());
        List<AdoptionResponse> adoptions = adoptionService.getMemberAdoptions(memberId);

        return CommonResponse.toResponseEntity(SuccessCode.ADOPTION_MY_LIST_SUCCESS, adoptions);
    }

    @Override
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<List<AdoptionResponse>>> allAdoptions() {
        List<AdoptionResponse> adoptions = adoptionService.getAllAdoptions();
        return CommonResponse.toResponseEntity(SuccessCode.ADOPTION_ALL_SUCCESS, adoptions);
    }

    @Override
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<Page<AdoptionResponse>>> getAdoptionList(Pageable pageable) {
        Page<AdoptionResponse> adoptions = adoptionService.getAllAdoptions(pageable);
        return CommonResponse.toResponseEntity(SuccessCode.ADOPTION_PAGE_SUCCESS, adoptions);
    }

    @Override
    @PutMapping("/{adoptionId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponse<AdoptionResponse>> updateStatus(
            @PathVariable Long adoptionId,
            @Valid @RequestBody AdoptionStatusUpdateRequest request
    ) {
        AdoptionResponse adoptionResponse = adoptionFacade.updateStatus(adoptionId, request.adoptionStatus());

        return CommonResponse.toResponseEntity(SuccessCode.ADOPTION_STATUS_UPDATE_SUCCESS, adoptionResponse);
    }
}
