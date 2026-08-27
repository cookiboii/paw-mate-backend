package com.kindtail.adoptmate.comment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kindtail.adoptmate.auth.JwtAuthFilter;
import com.kindtail.adoptmate.auth.JwtTokenProvider;
import com.kindtail.adoptmate.auth.TokenUserInfo;
import com.kindtail.adoptmate.comment.dto.CommentDto;
import com.kindtail.adoptmate.comment.dto.CommentResponseDto;
import com.kindtail.adoptmate.comment.dto.CommentUpdateDto;
import com.kindtail.adoptmate.comment.service.CommentService;
import com.kindtail.adoptmate.member.domain.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
@AutoConfigureMockMvc(addFilters = false)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private CommentResponseDto responseDto;

    @BeforeEach
    void setUp() {
        responseDto = new CommentResponseDto(
                1L, "댓글작성자", 10L, "commenter@example.com", "댓글 내용입니다.", LocalDateTime.now(), new ArrayList<>()
        );

        TokenUserInfo tokenUserInfo = TokenUserInfo.builder()
                .email("commenter@example.com")
                .role(Role.USER)
                .build();
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                tokenUserInfo, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("댓글을 등록할 수 있다 (200 OK)")
    void addComment_성공() throws Exception {
        // given
        Long postId = 1L;
        CommentDto requestDto = new CommentDto(null, "댓글 내용입니다.");
        given(commentService.addComment(eq(postId), any(CommentDto.class))).willReturn(responseDto);

        // when
        ResultActions resultActions = mockMvc.perform(post("/comment/{postId}", postId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)));

        // then
        resultActions.andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.statusMessage").value("댓글등록성공"))
                .andExpect(jsonPath("$.result.id").value(1))
                .andExpect(jsonPath("$.result.content").value("댓글 내용입니다."));
    }

    @Test
    @DisplayName("게시글의 댓글 목록을 조회할 수 있다 (200 OK)")
    void getComments_성공() throws Exception {
        // given
        Long postId = 1L;
        given(commentService.getComments(postId)).willReturn(List.of(responseDto));

        // when
        ResultActions resultActions = mockMvc.perform(get("/comment/{postId}", postId));

        // then
        resultActions.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.statusMessage").value("보기성공"))
                .andExpect(jsonPath("$.result[0].authorName").value("댓글작성자"));
    }

    @Test
    @DisplayName("댓글을 수정할 수 있다 (200 OK)")
    void updateComment_성공() throws Exception {
        // given
        Long commentId = 1L;
        CommentUpdateDto updateDto = new CommentUpdateDto(1L, "수정된 댓글");
        CommentResponseDto updatedResponse = new CommentResponseDto(
                1L, "댓글작성자", 10L, "commenter@example.com", "수정된 댓글", LocalDateTime.now(), new ArrayList<>()
        );

        given(commentService.updateComment(eq(commentId), any(CommentUpdateDto.class))).willReturn(updatedResponse);

        // when
        ResultActions resultActions = mockMvc.perform(put("/comment/update/{commentId}", commentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)));

        // then
        resultActions.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.statusMessage").value("수정성공"))
                .andExpect(jsonPath("$.result.content").value("수정된 댓글"));
    }

    @Test
    @DisplayName("댓글을 삭제할 수 있다 (200 OK)")
    void deleteComment_성공() throws Exception {
        // given
        Long commentId = 1L;
        doNothing().when(commentService).deleteComment(commentId);

        // when
        ResultActions resultActions = mockMvc.perform(delete("/comment/{commentId}", commentId));

        // then
        resultActions.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.statusMessage").value("댓글삭제성공"));
    }
}
