package com.kindtail.adoptmate.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kindtail.adoptmate.auth.JwtAuthFilter;
import com.kindtail.adoptmate.auth.JwtTokenProvider;
import com.kindtail.adoptmate.auth.TokenUserInfo;
import com.kindtail.adoptmate.common.exception.CustomException;
import com.kindtail.adoptmate.common.exception.ErrorCode;
import com.kindtail.adoptmate.config.SecurityConfig;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.domain.Role;
import com.kindtail.adoptmate.member.dto.*;
import com.kindtail.adoptmate.member.facade.MemberFacade;
import com.kindtail.adoptmate.member.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@WebMvcTest(MemberController.class)
@AutoConfigureMockMvc(addFilters = false)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MemberFacade memberFacade;

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .id(1L)
                .email("test@example.com")
                .name("테스트 사용자")
                .password("encodedPassword")
                .role(Role.USER)
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
    @DisplayName("회원을 등록할 수 있다")
    void registerMember_성공 () throws Exception {
        // given
        MemberRegisterRequestDto request = new MemberRegisterRequestDto(
                "나석후",
                "test@example.com",
                "password123",
                Role.USER
        );

        MemberResponseDto responseDto = MemberResponseDto.from(testMember);
        given(memberFacade.registerMember(any(MemberRegisterRequestDto.class))).willReturn(testMember);

        // when
        ResultActions resultActions = mockMvc.perform(post("/adoptmate/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        resultActions.andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.statusMessage").value("회원가입 성공"));
    }

    @Test
    @DisplayName("로그인할 수 있다")
    void login_성공 () throws Exception {
        // given
        MemberLoginRequestDto loginRequest = new MemberLoginRequestDto("test@example.com", "password123");
        MemberLoginResultDto result = new MemberLoginResultDto("accessToken123", "refreshToken123", "test@example.com", Role.USER);

        given(memberService.login(any(MemberLoginRequestDto.class))).willReturn(result);

        // when
        ResultActions resultActions = mockMvc.perform(post("/adoptmate/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)));

        // then
        resultActions.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusMessage").value("Login Success"))
                .andExpect(jsonPath("$.result.token").value("accessToken123"));
    }

    @Test
    @DisplayName("refresh token 으로 access token 을 재발급받을 수 있다")
    void refreshToken_성공 () throws Exception {
        // given
        Map<String, String> request = new HashMap<>();
        request.put("refreshToken", "refreshToken123");

        given(memberService.refreshAccessToken("refreshToken123")).willReturn("newAccessToken123");

        // when
        ResultActions resultActions = mockMvc.perform(post("/adoptmate/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        resultActions.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusMessage").value("토큰 재발급 성공"))
                .andExpect(jsonPath("$.result.token").value("newAccessToken123"));
    }

    @Test
    @DisplayName("내 정보를 조회할 수 있다")
    void getMyInfo_성공 () throws Exception {
        // given
        given(memberService.getMemberInfo()).willReturn(testMember);

        // when
        ResultActions resultActions = mockMvc.perform(get("/adoptmate/myInfo"));

        // then
        resultActions.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.email").value("test@example.com"))
                .andExpect(jsonPath("$.result.name").value("테스트 사용자"));
    }

    @Test
    @DisplayName("ADMIN 이 전체 회원을 조회할 수 있다")
    void getAllMembers_성공 () throws Exception {
        // given
        List<Member> members = List.of(testMember);
        given(memberService.getMembers()).willReturn(members);

        // when
        ResultActions resultActions = mockMvc.perform(get("/adoptmate/all"));

        // then
        resultActions.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusMessage").value("전체조회"))
                .andExpect(jsonPath("$.result[0].email").value("test@example.com"));
    }

    @Test
    @DisplayName("비밀번호를 변경할 수 있다")
    void changePassword_성공 () throws Exception {
        // given
        PasswordChangeRequestDto request = new PasswordChangeRequestDto("oldPassword", "newPassword");
        willDoNothing().given(memberService).changePassword(any(String.class), any(PasswordChangeRequestDto.class));

        // when
        ResultActions resultActions = mockMvc.perform(post("/adoptmate/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        resultActions.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusMessage").value("비밀번호 변경 완료"));
    }

    @Test
    @DisplayName("회원을 탈퇴할 수 있다")
    void deleteMember_성공 () throws Exception {
        // given
        doNothing().when(memberService).deleteUser(any(String.class), ArgumentMatchers.nullable(String.class));

        // when
        ResultActions resultActions = mockMvc.perform(delete("/adoptmate/delete"));

        // then
        resultActions.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusMessage").value("회원 탈퇴 완료"));
    }

    @Test
    @DisplayName("로그아웃할 수 있다")
    void logout_성공 () throws Exception {
        // given
        doNothing().when(memberService).logout(any(String.class));

        // when
        ResultActions resultActions = mockMvc.perform(post("/adoptmate/logout")
                .header("Authorization", "Bearer accessToken123"));

        // then
        resultActions.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusMessage").value("로그아웃 성공"));
    }
}
