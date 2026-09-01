package com.kindtail.adoptmate.animal.controller;

import com.kindtail.adoptmate.animal.domain.Species;
import com.kindtail.adoptmate.animal.dto.AnimalCreateRequest;
import com.kindtail.adoptmate.animal.dto.AnimalStatusUpdateRequest;
import com.kindtail.adoptmate.common.dto.CommonResDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "4. 보호 동물 관리 API", description = "보호 동물 등록, 상세 조회, 페이징 목록 조회, 상태 변경 및 삭제 API")
public interface AnimalControllerDocs {

    @Operation(summary = "보호 동물 등록 (관리자 전용)", description = "새로운 유기/보호 동물을 시스템에 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "동물 등록 성공"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    ResponseEntity<CommonResDto> adoptAnimal(@Valid @RequestBody AnimalCreateRequest animalCreateRequest);

    @Operation(summary = "보호 동물 전체 목록 조회 (페이징)", description = "등록된 전체 보호 동물 목록을 페이징하여 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "동물 목록 조회 성공")
    })
    ResponseEntity<CommonResDto> getAnimalList(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10") @RequestParam(defaultValue = "10") int size
    );

    @Operation(summary = "보호 동물 전체 목록 조회 (No-Offset 커서 / 무한 스크롤)", description = "lastAnimalId를 기준으로 다음 페이지의 동물 목록을 No-Offset 방식으로 조회하여 count 쿼리 오버헤드 없이 고속 페이징합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "동물 목록 조회 성공")
    })
    ResponseEntity<CommonResDto> getAnimalsByCursor(
            @Parameter(description = "마지막으로 조회된 동물 ID (첫 페이지 요청 시 생략 또는 null)", example = "10")
            @RequestParam(required = false) Long lastAnimalId,
            @Parameter(description = "조회할 동물 수 (기본값: 10)", example = "10")
            @RequestParam(defaultValue = "10") int size
    );

    @Operation(summary = "종별 보호 동물 목록 조회 (페이징)", description = "종(DOG, CAT, OTHER)별로 보호 동물 목록을 페이징하여 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "종별 동물 목록 조회 성공")
    })
    ResponseEntity<CommonResDto> getAnimalsBySpecies(
            @Parameter(description = "동물 종 (DOG, CAT, OTHER)", example = "DOG") @RequestParam Species species,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10") @RequestParam(defaultValue = "10") int size
    );

    @Operation(summary = "보호 동물 상세 조회", description = "동물 ID로 상세 프로필 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상세 조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 동물")
    })
    ResponseEntity<CommonResDto> getAnimalById(
            @Parameter(description = "동물 ID", example = "1") @PathVariable Long id
    );

    @Operation(summary = "보호 동물 상태 변경 (관리자 전용)", description = "보호 동물의 상태(PROTECTED, WAITING, ADOPTED)를 갱신합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상태 변경 성공"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 동물")
    })
    ResponseEntity<CommonResDto> updateAnimal(
            @Parameter(description = "동물 ID", example = "1") @PathVariable Long id,
            @Valid @RequestBody AnimalStatusUpdateRequest request
    );

    @Operation(summary = "보호 동물 삭제 (관리자 전용)", description = "보호 동물을 시스템에서 논리 삭제(Soft Delete)합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 동물")
    })
    ResponseEntity<CommonResDto> deleteAnimal(
            @Parameter(description = "동물 ID", example = "1") @PathVariable Long id
    );
}
