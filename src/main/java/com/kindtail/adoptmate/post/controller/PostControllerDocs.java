package com.kindtail.adoptmate.post.controller;

import com.kindtail.adoptmate.common.dto.CommonResDto;
import com.kindtail.adoptmate.post.dto.PostCreateRequestDto;
import com.kindtail.adoptmate.post.dto.PostUpdateRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "6. 커뮤니티 게시글 API", description = "입양 후기 및 자유 게시글 작성, 목록 조회(페이징), 상세 조회, 수정, 삭제 API")
public interface PostControllerDocs {

    @Operation(summary = "게시글 작성", description = "새로운 커뮤니티 게시글을 작성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "글 작성 완료"),
            @ApiResponse(responseCode = "400", description = "유효성 검사 실패"),
            @ApiResponse(responseCode = "401", description = "로그인 필요")
    })
    ResponseEntity<CommonResDto> createPost(@Valid @RequestBody PostCreateRequestDto dto);

    @Operation(summary = "게시글 목록 조회 (페이징)", description = "커뮤니티 게시글 목록을 페이징하여 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 완료")
    })
    ResponseEntity<CommonResDto> getPostList(Pageable pageable);

    @Operation(summary = "게시글 상세 조회", description = "게시글 ID로 상세 내용 및 작성자 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 완료"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 게시글")
    })
    ResponseEntity<CommonResDto> getPostById(
            @Parameter(description = "게시글 ID", example = "1") @PathVariable Long postId
    );

    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다. (작성자 본인 또는 관리자만 가능)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 완료"),
            @ApiResponse(responseCode = "403", description = "수정/삭제 권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 게시글")
    })
    ResponseEntity<CommonResDto> deletePostById(
            @Parameter(description = "게시글 ID", example = "1") @PathVariable Long postId
    );

    @Operation(summary = "게시글 수정", description = "게시글 제목, 내용, 이미지를 수정합니다. (작성자 본인 또는 관리자만 가능)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "글 수정 완료"),
            @ApiResponse(responseCode = "403", description = "수정/삭제 권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 게시글")
    })
    ResponseEntity<CommonResDto> updatePost(
            @Parameter(description = "게시글 ID", example = "1") @PathVariable Long postId,
            @Valid @RequestBody PostUpdateRequestDto dto
    );
}
