package com.kindtail.adoptmate.animal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kindtail.adoptmate.animal.domain.Animal;
import com.kindtail.adoptmate.animal.domain.Gender;
import com.kindtail.adoptmate.animal.domain.Species;
import com.kindtail.adoptmate.animal.domain.Status;
import com.kindtail.adoptmate.animal.dto.AnimalCreateRequest;
import com.kindtail.adoptmate.animal.dto.AnimalResponse;
import com.kindtail.adoptmate.animal.dto.AnimalStatusUpdateRequest;
import com.kindtail.adoptmate.animal.service.AnimalService;
import com.kindtail.adoptmate.auth.JwtAuthFilter;
import com.kindtail.adoptmate.auth.JwtTokenProvider;
import com.kindtail.adoptmate.common.exception.CustomException;
import com.kindtail.adoptmate.auth.CustomUserDetails;
import com.kindtail.adoptmate.common.exception.ErrorCode;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.domain.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

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

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@WebMvcTest(AnimalController.class)
@AutoConfigureMockMvc(addFilters = false)
class AnimalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AnimalService animalService;

    @MockitoBean
    private com.kindtail.adoptmate.animal.service.AnimalFavoriteService animalFavoriteService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private Animal testAnimal;

    @BeforeEach
    void setUp() {
        testAnimal = Animal.builder()
                .id(1L)
                .species(Species.DOG)
                .breed("진도개")
                .color("황색")
                .gender(Gender.MALE)
                .age(3L)
                .image("http://example.com/image.jpg")
                .status(Status.PROTECTED)
                .build();

        Member testMember = Member.builder()
                .id(1L)
                .email("test@example.com")
                .name("테스트 사용자")
                .role(Role.USER)
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(testMember);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("동물을 등록할 수 있다 (ADMIN 권한)")
    void registerAnimal_성공 () throws Exception {
        // given
        AnimalCreateRequest request = new AnimalCreateRequest(
                Species.DOG,
                "진도개",
                "황색",
                "http://example.com/image.jpg",
                3L,
                Gender.MALE,
                Status.PROTECTED
        );

        given(animalService.registerAnimal(any(AnimalCreateRequest.class))).willReturn(AnimalResponse.from(testAnimal));

        // when
        ResultActions resultActions = mockMvc.perform(post("/animals/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        resultActions.andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.statusMessage").value("등록 성공"))
                .andExpect(jsonPath("$.result.species").value("DOG"));
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
                .andExpect(jsonPath("$.result.content[0].species").value("DOG"));
    }

    @Test
    @DisplayName("No-Offset 커서 기반으로 동물 목록을 조회할 수 있다")
    void getAnimalsByCursor_성공() throws Exception {
        // given
        List<AnimalResponse> animalResponses = new ArrayList<>();
        animalResponses.add(AnimalResponse.from(testAnimal));
        org.springframework.data.domain.Slice<AnimalResponse> slice = new org.springframework.data.domain.SliceImpl<>(animalResponses, PageRequest.of(0, 10), false);

        given(animalService.getAnimalsByCursor(eq(10L), eq(10))).willReturn(slice);

        // when
        ResultActions resultActions = mockMvc.perform(get("/animals/cursor")
                .param("lastAnimalId", "10")
                .param("size", "10"));

        // then
        resultActions.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.content[0].species").value("DOG"));
    }

    @Test
    @DisplayName("종별로 페이지네이션된 동물 목록을 조회할 수 있다")
    void getAnimalsBySpecies_성공 () throws Exception {
        // given
        List<AnimalResponse> animalResponses = new ArrayList<>();
        animalResponses.add(AnimalResponse.from(testAnimal));
        Page<AnimalResponse> animalPage = new PageImpl<>(animalResponses, PageRequest.of(0, 10), 1);

        given(animalService.getAnimalsBySpecies(eq(Species.DOG), any(PageRequest.class))).willReturn(animalPage);

        // when
        ResultActions resultActions = mockMvc.perform(get("/animals/species")
                .param("species", "DOG")
                .param("page", "0")
                .param("size", "10"));

        // then
        resultActions.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.content[0].species").value("DOG"));
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
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.statusMessage").value("상세 조회 성공"))
                .andExpect(jsonPath("$.result.species").value("DOG"));
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
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        resultActions.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusMessage").value("상태가 성공적으로 변경되었습니다."));
    }

    @Test
    @DisplayName("동물을 삭제할 수 있다 (ADMIN 권한)")
    void deleteAnimal_성공 () throws Exception {
        // given
        Long animalId = 1L;
        doNothing().when(animalService).deleteAnimal(animalId);

        // when
        ResultActions resultActions = mockMvc.perform(delete("/animals/delete/{id}", animalId));

        // then
        resultActions.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusMessage").value("삭제 성공"));
    }

    @Test
    @DisplayName("관심 동물 찜하기를 토글할 수 있다")
    void toggleFavorite_성공 () throws Exception {
        // given
        Long animalId = 1L;
        com.kindtail.adoptmate.animal.dto.FavoriteToggleResponseDto responseDto =
                new com.kindtail.adoptmate.animal.dto.FavoriteToggleResponseDto(animalId, true, 1L);

        given(animalFavoriteService.toggleFavorite(eq(animalId), any())).willReturn(responseDto);

        // when
        ResultActions resultActions = mockMvc.perform(post("/animals/{id}/favorite", animalId));

        // then
        resultActions.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.statusMessage").value("관심 동물 상태가 성공적으로 변경되었습니다."))
                .andExpect(jsonPath("$.result.isFavorite").value(true))
                .andExpect(jsonPath("$.result.favoriteCount").value(1));
    }

    @Test
    @DisplayName("내가 찜한 동물 목록을 조회할 수 있다")
    void getMyFavoriteAnimals_성공 () throws Exception {
        // given
        Animal animal = Animal.builder()
                .id(1L)
                .species(Species.DOG)
                .breed("말티즈")
                .color("흰색")
                .status(Status.PROTECTED)
                .age(2L)
                .gender(Gender.MALE)
                .image("image.jpg")
                .build();
        AnimalResponse animalResponse = AnimalResponse.from(animal);
        Page<AnimalResponse> page = new PageImpl<>(List.of(animalResponse), PageRequest.of(0, 10), 1);

        given(animalFavoriteService.getMyFavoriteAnimals(any(), any())).willReturn(page);

        // when
        ResultActions resultActions = mockMvc.perform(get("/animals/favorites/my"));

        // then
        resultActions.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.statusMessage").value("관심 동물 목록 조회 성공"))
                .andExpect(jsonPath("$.result.content[0].breed").value("말티즈"));
    }

    @Test
    @DisplayName("관심 동물 찜을 명시적으로 삭제할 수 있다")
    void deleteFavorite_성공 () throws Exception {
        // given
        Long animalId = 1L;
        com.kindtail.adoptmate.animal.dto.FavoriteToggleResponseDto responseDto =
                new com.kindtail.adoptmate.animal.dto.FavoriteToggleResponseDto(animalId, false, 0L);

        given(animalFavoriteService.removeFavorite(eq(animalId), any())).willReturn(responseDto);

        // when
        ResultActions resultActions = mockMvc.perform(delete("/animals/{id}/favorite", animalId));

        // then
        resultActions.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.statusMessage").value("관심 동물이 찜 목록에서 삭제되었습니다."))
                .andExpect(jsonPath("$.result.isFavorite").value(false))
                .andExpect(jsonPath("$.result.favoriteCount").value(0));
    }
}
