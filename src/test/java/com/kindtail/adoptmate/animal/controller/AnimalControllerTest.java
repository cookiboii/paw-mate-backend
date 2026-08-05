package com.kindtail.adoptmate.animal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kindtail.adoptmate.animal.domain.Animal;
import com.kindtail.adoptmate.animal.domain.Gender;
import com.kindtail.adoptmate.animal.domain.Status;
import com.kindtail.adoptmate.animal.dto.AnimalCreateRequest;
import com.kindtail.adoptmate.animal.dto.AnimalResponse;
import com.kindtail.adoptmate.animal.dto.AnimalStatusUpdateRequest;
import com.kindtail.adoptmate.animal.service.AnimalService;
import com.kindtail.adoptmate.auth.JwtAuthFilter;
import com.kindtail.adoptmate.auth.TokenUserInfo;
import com.kindtail.adoptmate.common.exception.CustomException;
import com.kindtail.adoptmate.common.exception.ErrorCode;
import com.kindtail.adoptmate.config.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnimalController.class)
@Import(SecurityConfig.class)
class AnimalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AnimalService animalService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    private Animal testAnimal;

    @BeforeEach
    void setUp() {
        testAnimal = Animal.builder()
                .id(1L)
                .species("강아지")
                .breed("진도개")
                .color("황색")
                .gender(Gender.MALE)
                .age(3L)
                .image("http://example.com/image.jpg")
                .status(Status.PROTECTED)
                .build();
    }

    @Test
    @DisplayName("동물을 등록할 수 있다 (ADMIN 권한)")
    void registerAnimal_성공 () throws Exception {
        // given
        AnimalCreateRequest request = new AnimalCreateRequest(
                "강아지",
                "진도개",
                "황색",
                "http://example.com/image.jpg",
                3L,
                Gender.MALE,
                Status.PROTECTED,
                null
        );

        given(animalService.registerAnimal(any(AnimalCreateRequest.class))).willReturn(testAnimal);

        // when
        ResultActions resultActions = mockMvc.perform(post("/animals/register")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        resultActions.andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("등록 성공"))
                .andExpect(jsonPath("$.data.species").value("강아지"));
    }

    @Test
    @DisplayName("페이지네이션으로 동물 목록을 조회할 수 있다")
    void getAnimalList_성공 () throws Exception {
        // given
        List<AnimalResponse> animalResponses = new ArrayList<>();
        animalResponses.add(AnimalResponse.from(testAnimal));
        Page<AnimalResponse> animalPage = new PageImpl<>(animalResponses, PageRequest.of(0, 10), 1);

        given(animalService.getAllAnimals(any(PageRequest.class))).willReturn(animalPage);

        // when
        ResultActions resultActions = mockMvc.perform(get("/animals/list")
                .param("page", "0")
                .param("size", "10"));

        // then
        resultActions.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].species").value("강아지"));
    }

    @Test
    @DisplayName("ID 로 동물을 상세 조회할 수 있다")
    void getAnimalById_성공 () throws Exception {
        // given
        Long animalId = 1L;
        AnimalResponse response = AnimalResponse.from(testAnimal);
        given(animalService.getAnimal(animalId)).willReturn(response);

        // when
        ResultActions resultActions = mockMvc.perform(get("/animals/{id}", animalId));

        // then
        resultActions.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("상세 조회 성공"))
                .andExpect(jsonPath("$.data.species").value("강아지"));
    }

    @Test
    @DisplayName("존재하지 않는 동물을 조회하면 404 에러가 발생한다")
    void getAnimalById_없음_예외 () throws Exception {
        // given
        Long animalId = 999L;
        given(animalService.getAnimal(animalId))
                .willThrow(new CustomException(ErrorCode.ANIMAL_NOT_FOUND));

        // when
        ResultActions resultActions = mockMvc.perform(get("/animals/{id}", animalId));

        // then
        resultActions.andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("동물 상태를 업데이트할 수 있다 (ADMIN 권한)")
    void updateAnimal_성공 () throws Exception {
        // given
        Long animalId = 1L;
        AnimalStatusUpdateRequest request = new AnimalStatusUpdateRequest(Status.ADOPTED);
        AnimalResponse response = AnimalResponse.from(testAnimal);
        
        given(animalService.updateAnimal(any(Long.class), any(AnimalStatusUpdateRequest.class)))
                .willReturn(response);

        // when
        ResultActions resultActions = mockMvc.perform(put("/animals/{id}/status", animalId)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        resultActions.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("상태가 성공적으로 변경되었습니다."));
    }

    @Test
    @DisplayName("동물을 삭제할 수 있다 (ADMIN 권한)")
    void deleteAnimal_성공 () throws Exception {
        // given
        Long animalId = 1L;
        doNothing().when(animalService).deleteAnimal(animalId);

        // when
        ResultActions resultActions = mockMvc.perform(delete("/animals/delete/{id}", animalId)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))));

        // then
        resultActions.andDo(print())
                .andExpect(status().isNoContent());
    }
}
