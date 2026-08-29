package com.kindtail.adoptmate.adoption.controller;

import com.kindtail.adoptmate.adoption.dto.AdoptionCreateRequest;
import com.kindtail.adoptmate.adoption.dto.AdoptionResponseDto;
import com.kindtail.adoptmate.adoption.dto.AdoptionUpdateRequestDto;
import com.kindtail.adoptmate.auth.TokenUserInfo;
import com.kindtail.adoptmate.common.dto.CommonResDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "5. 입양 신청 관리 API", description = "입양 신청서 제출(분산 락), 내 신청 내역 조회, 관리자 심사/승인/반려(상태 머신) API")
public interface AdoptionControllerDocs {

    @Operation(summary = "동물 입양 신청서 제출", description = "보호 중인 동물에게 입양 신청서를 제출합니다. (동일 동물 분산 락 및 중복 방지)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "입양 신청 완료"),
            @ApiResponse(responseCode = "400", description = "이미 신청한 동물 또는 보호 중이지 않은 동물"),
            @ApiResponse(responseCode = "409", description = "동시 신청 요청 집중 충돌")
    })
    ResponseEntity<CommonResDto> registerAdoption(
            @Parameter(description = "신청 대상 동물 ID", example = "1") @PathVariable("animalId") Long animalId,
            @Valid @RequestBody AdoptionCreateRequest adoptionCreateRequest,
            @AuthenticationPrincipal TokenUserInfo userInfo
    );

    @Operation(summary = "내 입양 신청 내역 조회", description = "현재 로그인한 회원이 신청한 모든 입양 내역을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "내 입양 내역 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    ResponseEntity<CommonResDto> myAdoption(@AuthenticationPrincipal TokenUserInfo userInfo);

    @Operation(summary = "전체 입양 신청 내역 조회 (리스트 - 관리자 전용)", description = "관리자 권한으로 전체 회원의 입양 신청 내역을 리스트로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "전체 조회 성공"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    ResponseEntity<CommonResDto> allAdoptions();

    @Operation(summary = "전체 입양 신청 내역 조회 (페이징 - 관리자 전용)", description = "관리자 권한으로 전체 입양 신청 내역을 페이징하여 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "전체 입양 목록 조회 성공"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    ResponseEntity<CommonResDto> getAdoptionList(Pageable pageable);

    @Operation(summary = "입양 신청 상태 변경 (승인/반려 - 관리자 전용)", description = "입양 신청을 승인(APPROVED) 또는 반려(REJECTED)합니다. 승인 시 타 신청건 연쇄 반려 및 동물 락 동기화가 수행됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상태 변경 완료"),
            @ApiResponse(responseCode = "400", description = "대기(PENDING) 상태가 아니거나 잘못된 상태 전이"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 입양 신청")
    })
    ResponseEntity<CommonResDto> updateStatus(
            @Parameter(description = "입양 신청 ID", example = "1") @PathVariable Long adoptionId,
            @Valid @RequestBody AdoptionUpdateRequestDto requestDto
    );
}
