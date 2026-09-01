package com.kindtail.adoptmate.post.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kindtail.adoptmate.auth.JwtAuthFilter;
import com.kindtail.adoptmate.auth.JwtTokenProvider;
import com.kindtail.adoptmate.auth.TokenUserInfo;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.domain.Role;
import com.kindtail.adoptmate.post.domain.Post;
import com.kindtail.adoptmate.post.dto.PostCreateRequestDto;
import com.kindtail.adoptmate.post.dto.PostResponseDto;
import com.kindtail.adoptmate.post.dto.PostUpdateRequestDto;
import com.kindtail.adoptmate.post.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class)
@AutoConfigureMockMvc(addFilters = false)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PostService postService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private Post testPost;
    private Member author;

    @BeforeEach
    void setUp() {
        author = Member.builder()
                .id(1L)
                .email("test@example.com")
                .name("테스트 사용자")
                .role(Role.USER)
                .build();

        testPost = Post.builder()
                .id(1L)
                .title("테스트 제목")
                .content("테스트 내용")
                .image("test.jpg")
                .member(author)
                .build();

        TokenUserInfo tokenUserInfo = TokenUserInfo.builder()
                .email("test@example.com")
                .role(Role.USER)
                .build();
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                tokenUserInfo, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("게시글을 성공적으로 작성할 수 있다 (201 CREATED)")
    void createPost_성공() throws Exception {
        // given
        PostCreateRequestDto requestDto = new PostCreateRequestDto("테스트 제목", "테스트 내용", "test.jpg");
        given(postService.createPost(any(PostCreateRequestDto.class))).willReturn(testPost);

        // when
        ResultActions resultActions = mockMvc.perform(post("/post/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)));

        // then
        resultActions.andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.statusMessage").value("글쓰기완료"))
                .andExpect(jsonPath("$.result.title").value("테스트 제목"));
    }

    @Test
    @DisplayName("게시글 목록을 페이지네이션으로 조회할 수 있다 (200 OK)")
    void getPostList_성공() throws Exception {
        // given
        PostResponseDto responseDto = PostResponseDto.from(testPost);
        Page<PostResponseDto> page = new PageImpl<>(List.of(responseDto), PageRequest.of(0, 10), 1);
        given(postService.getAllPosts(any(PageRequest.class))).willReturn(page);

        // when
        ResultActions resultActions = mockMvc.perform(get("/post/list")
                .param("page", "0")
                .param("size", "10"));

        // then
        resultActions.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.statusMessage").value("조회완료"))
                .andExpect(jsonPath("$.result.content[0].title").value("테스트 제목"));
    }

    @Test
    @DisplayName("게시글 목록을 No-Offset 커서 기반으로 조회할 수 있다 (200 OK)")
    void getPostsByCursor_성공() throws Exception {
        // given
        PostResponseDto responseDto = PostResponseDto.from(testPost);
        org.springframework.data.domain.Slice<PostResponseDto> slice = new org.springframework.data.domain.SliceImpl<>(List.of(responseDto), PageRequest.of(0, 10), false);
        given(postService.getPostsByCursor(eq(10L), eq(10))).willReturn(slice);

        // when
        ResultActions resultActions = mockMvc.perform(get("/post/cursor")
                .param("lastPostId", "10")
                .param("size", "10"));

        // then
        resultActions.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.statusMessage").value("조회완료"))
                .andExpect(jsonPath("$.result.content[0].title").value("테스트 제목"));
    }

    @Test
    @DisplayName("postId 로 게시글 상세 조회할 수 있다 (200 OK)")
    void getPostById_성공() throws Exception {
        // given
        Long postId = 1L;
        PostResponseDto responseDto = PostResponseDto.from(testPost);
        given(postService.getPost(postId)).willReturn(responseDto);

        // when
        ResultActions resultActions = mockMvc.perform(get("/post/{postId}", postId));

        // then
        resultActions.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.statusMessage").value("조회완료"))
                .andExpect(jsonPath("$.result.id").value(1));
    }

    @Test
    @DisplayName("게시글을 수정할 수 있다 (200 OK)")
    void updatePost_성공() throws Exception {
        // given
        Long postId = 1L;
        PostUpdateRequestDto requestDto = new PostUpdateRequestDto("수정 제목", "new.jpg", "수정 내용");
        PostResponseDto updatedResponse = new PostResponseDto(1L, "수정 제목", "수정 내용", "test@example.com", "테스트 사용자", LocalDateTime.now(), "new.jpg");

        given(postService.updatePost(eq(postId), any(PostUpdateRequestDto.class))).willReturn(updatedResponse);

        // when
        ResultActions resultActions = mockMvc.perform(put("/post/{postId}", postId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)));

        // then
        resultActions.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.statusMessage").value("글수정완료"))
                .andExpect(jsonPath("$.result.title").value("수정 제목"));
    }

    @Test
    @DisplayName("게시글을 삭제할 수 있다 (200 OK)")
    void deletePostById_성공() throws Exception {
        // given
        Long postId = 1L;
        doNothing().when(postService).deletePost(postId);

        // when
        ResultActions resultActions = mockMvc.perform(delete("/post/{postId}", postId));

        // then
        resultActions.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.statusMessage").value("삭제완료"));
    }
}
