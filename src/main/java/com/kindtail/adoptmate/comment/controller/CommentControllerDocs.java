package com.kindtail.adoptmate.comment.controller;

import com.kindtail.adoptmate.comment.dto.CommentDto;
import com.kindtail.adoptmate.comment.dto.CommentResponseDto;
import com.kindtail.adoptmate.comment.dto.CommentUpdateDto;
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

import java.util.List;

@Tag(name = "7. 댓글 & 계층형 대댓글 API", description = "게시글 댓글 작성, 계층형 대댓글 트리 목록 조회, 수정, 삭제 API")
public interface CommentControllerDocs {

    @Operation(summary = "댓글 또는 대댓글 작성", description = "게시글에 최상위 댓글을 작성하거나, parentId를 지정하여 특정 댓글에 대댓글을 작성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "댓글 등록 성공"),
            @ApiResponse(responseCode = "400", description = "유효성 검사 실패"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 게시글 또는 부모 댓글")
    })
    ResponseEntity<CommonResDto> addComment(
            @Parameter(description = "게시글 ID", example = "1") @PathVariable Long postId,
            @Valid @RequestBody CommentDto commentDto
    );

    @Operation(summary = "게시글 댓글 목록 조회 (계층형 대댓글 트리)", description = "특정 게시글의 모든 댓글과 자식 답글들을 계층형 트리 구조로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 목록 조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 게시글")
    })
    ResponseEntity<CommonResDto> getComments(
            @Parameter(description = "게시글 ID", example = "1") @PathVariable Long postId
    );

    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다. (작성자 본인 또는 관리자만 가능)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 삭제 성공"),
            @ApiResponse(responseCode = "403", description = "수정/삭제 권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 댓글")
    })
    ResponseEntity<CommonResDto> deleteComment(
            @Parameter(description = "댓글 ID", example = "1") @PathVariable Long commentId
    );

    @Operation(summary = "댓글 수정", description = "댓글 내용을 수정합니다. (작성자 본인 또는 관리자만 가능)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 수정 성공"),
            @ApiResponse(responseCode = "403", description = "수정/삭제 권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 댓글")
    })
    ResponseEntity<CommonResDto> updateComment(
            @Parameter(description = "댓글 ID", example = "1") @PathVariable Long commentId,
            @Valid @RequestBody CommentUpdateDto dto
    );
}
