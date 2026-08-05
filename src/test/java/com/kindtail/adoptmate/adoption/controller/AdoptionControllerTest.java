package com.kindtail.adoptmate.adoption.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kindtail.adoptmate.adoption.domain.Adoption;
import com.kindtail.adoptmate.adoption.domain.AdoptionStatus;
import com.kindtail.adoptmate.adoption.dto.AdoptionRequestDto;
import com.kindtail.adoptmate.adoption.dto.AdoptionResponseDto;
import com.kindtail.adoptmate.adoption.dto.AdoptionUpdateRequestDto;
import com.kindtail.adoptmate.adoption.service.AdoptionService;
import com.kindtail.adoptmate.animal.domain.Animal;
import com.kindtail.adoptmate.animal.domain.Status;
import com.kindtail.adoptmate.auth.TokenUserInfo;
import com.kindtail.adoptmate.common.dto.CommonResDto;
import com.kindtail.adoptmate.auth.JwtAuthFilter;
import com.kindtail.adoptmate.auth.JwtTokenProvider;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.domain.Role;
import com.kindtail.adoptmate.member.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdoptionController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdoptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdoptionService adoptionService;

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private TokenUserInfo tokenUserInfo;

    @BeforeEach
    void setUp() {
        tokenUserInfo = TokenUserInfo.builder()
                .email("test@example.com")
                .role(Role.USER)
                .build();
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                tokenUserInfo, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("입양 신청을 성공적으로 처리한다 (201 ACCEPTED)")
    @WithMockUser
    void registerAdoptionSuccess() throws Exception {
        // given
        Long animalId = 1L;
        Long memberId = 1L;
        AdoptionRequestDto requestDto = new AdoptionRequestDto(null, null, "인터뷰 내용", null);
        AdoptionResponseDto responseDto = new AdoptionResponseDto(1L, "홍길동", AdoptionStatus.PENDING, "인터뷰 내용", "test.jpg", "2024-01-01T00:00:00");

        given(memberService.getMemberIdByEmail(anyString())).willReturn(memberId);
        given(adoptionService.applyAdoption(any(AdoptionRequestDto.class), eq(memberId), eq(animalId)))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(post("/adoptions/animals/{animalId}", animalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(202))
                .andExpect(jsonPath("$.statusMessage").value("입양 신청이 완료되었습니다."))
                .andExpect(jsonPath("$.result.adoptionId").value(1))
                .andDo(print());
    }

    @Test
    @DisplayName("내 입양 내역을 조회한다 (200 OK)")
    @WithMockUser
    void myAdoptionSuccess() throws Exception {
        // given
        Long memberId = 1L;
        AdoptionResponseDto responseDto1 = new AdoptionResponseDto(1L, "홍길동", AdoptionStatus.PENDING, "인터뷰 1", "test.jpg", "2024-01-01T00:00:00");
        AdoptionResponseDto responseDto2 = new AdoptionResponseDto(2L, "홍길동", AdoptionStatus.APPROVED, "인터뷰 2", "test2.jpg", "2024-01-02T00:00:00");

        given(memberService.getMemberIdByEmail(anyString())).willReturn(memberId);
        given(adoptionService.getAdoptions(memberId)).willReturn(List.of(responseDto1, responseDto2));

        // when & then
        mockMvc.perform(get("/adoptions/myAdoption")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.statusMessage").value("내 입양 내역 조회 성공"))
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result.length()").value(2))
                .andDo(print());
    }

    @Test
    @DisplayName("전체 입양 내역을 조회한다 (200 OK)")
    @WithMockUser
    void allAdoptionsSuccess() throws Exception {
        // given
        AdoptionResponseDto responseDto1 = new AdoptionResponseDto(1L, "홍길동", AdoptionStatus.PENDING, "인터뷰 1", "test.jpg", "2024-01-01T00:00:00");
        AdoptionResponseDto responseDto2 = new AdoptionResponseDto(2L, "김철수", AdoptionStatus.APPROVED, "인터뷰 2", "test2.jpg", "2024-01-02T00:00:00");

        given(adoptionService.getAllAdoptions()).willReturn(List.of(responseDto1, responseDto2));

        // when & then
        mockMvc.perform(get("/adoptions/all")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.statusMessage").value("전체조회"))
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result.length()").value(2))
                .andDo(print());
    }

    @Test
    @DisplayName("입양 상태를 변경한다 (200 OK)")
    @WithMockUser
    void updateStatusSuccess() throws Exception {
        // given
        Long adoptionId = 1L;
        AdoptionUpdateRequestDto requestDto = new AdoptionUpdateRequestDto(AdoptionStatus.APPROVED);
        AdoptionResponseDto responseDto = new AdoptionResponseDto(1L, "홍길동", AdoptionStatus.APPROVED, "인터뷰", "test.jpg", "2024-01-01T00:00:00");

        given(adoptionService.updateStatus(eq(adoptionId), eq(AdoptionStatus.APPROVED)))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(put("/adoptions/{adoptionId}/status", adoptionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.statusMessage").value("상태변경완료"))
                .andExpect(jsonPath("$.result.adoptionId").value(1))
                .andExpect(jsonPath("$.result.status").value("APPROVED"))
                .andDo(print());
    }
}
