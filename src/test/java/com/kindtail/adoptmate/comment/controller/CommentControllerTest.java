package com.kindtail.adoptmate.comment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kindtail.adoptmate.auth.CustomUserDetails;
import com.kindtail.adoptmate.auth.JwtAuthFilter;
import com.kindtail.adoptmate.auth.JwtTokenProvider;
import com.kindtail.adoptmate.comment.dto.CommentCreateRequest;
import com.kindtail.adoptmate.comment.dto.CommentResponse;
import com.kindtail.adoptmate.comment.dto.CommentUpdateRequest;
import com.kindtail.adoptmate.comment.service.CommentService;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.domain.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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

    private CommentResponse response;

    @BeforeEach
    void setUp() {
        response = new CommentResponse(
                1L, "댓글작성자", 10L, "commenter@example.com", "댓글 내용입니다.", LocalDateTime.now(), new ArrayList<>()
        );

        Member member = Member.builder()
                .id(1L)
                .email("commenter@example.com")
                .name("댓글작성자")
                .role(Role.USER)
                .build();
        CustomUserDetails userDetails = new CustomUserDetails(member);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("댓글을 등록할 수 있다 (200 OK)")
    void createComment_성공() throws Exception {
        // given
        Long postId = 1L;
        CommentCreateRequest request = new CommentCreateRequest(null, "댓글 내용입니다.");
        given(commentService.createComment(eq(postId), any(CommentCreateRequest.class))).willReturn(response);

        // when
        ResultActions resultActions = mockMvc.perform(post("/comment/{postId}", postId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

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
        given(commentService.getComments(eq(postId), any())).willReturn(
                new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1)
        );

        // when
        ResultActions resultActions = mockMvc.perform(get("/comment/{postId}", postId)
                .param("page", "0")
                .param("size", "20"));

        // then
        resultActions.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.statusMessage").value("보기성공"))
                .andExpect(jsonPath("$.result.content[0].authorName").value("댓글작성자"))
                .andExpect(jsonPath("$.result.totalElements").value(1));
    }

    @Test
    @DisplayName("댓글을 수정할 수 있다 (200 OK)")
    void updateComment_성공() throws Exception {
        // given
        Long commentId = 1L;
        CommentUpdateRequest request = new CommentUpdateRequest(1L, "수정된 댓글");
        CommentResponse updatedResponse = new CommentResponse(
                1L, "댓글작성자", 10L, "commenter@example.com", "수정된 댓글", LocalDateTime.now(), new ArrayList<>()
        );

        given(commentService.updateComment(eq(commentId), any(CommentUpdateRequest.class))).willReturn(updatedResponse);

        // when
        ResultActions resultActions = mockMvc.perform(put("/comment/update/{commentId}", commentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

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
