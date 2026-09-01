package com.kindtail.adoptmate.post.service;

import com.kindtail.adoptmate.auth.TokenUserInfo;
import com.kindtail.adoptmate.common.exception.CustomException;
import com.kindtail.adoptmate.common.exception.ErrorCode;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.domain.Role;
import com.kindtail.adoptmate.member.repository.MemberRepository;
import com.kindtail.adoptmate.post.domain.Post;
import com.kindtail.adoptmate.post.dto.PostCreateRequestDto;
import com.kindtail.adoptmate.post.dto.PostResponseDto;
import com.kindtail.adoptmate.post.dto.PostUpdateRequestDto;
import com.kindtail.adoptmate.post.repository.PostRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private PostService postService;

    private Member author;
    private Member anotherUser;
    private Post testPost;

    @BeforeEach
    void setUp() {
        author = Member.builder()
                .id(1L)
                .email("author@example.com")
                .name("작성자")
                .role(Role.USER)
                .build();

        anotherUser = Member.builder()
                .id(2L)
                .email("other@example.com")
                .name("다른사용자")
                .role(Role.USER)
                .build();

        testPost = Post.builder()
                .id(10L)
                .title("테스트 제목")
                .content("테스트 내용")
                .image("http://example.com/image.jpg")
                .member(author)
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setupSecurityContext(String email, Role role) {
        TokenUserInfo tokenUserInfo = TokenUserInfo.builder()
                .email(email)
                .role(role)
                .build();
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                tokenUserInfo, null, List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("로그인한 회원이 게시글을 성공적으로 등록할 수 있다")
    void createPost_성공() {
        // given
        setupSecurityContext("author@example.com", Role.USER);
        PostCreateRequestDto request = new PostCreateRequestDto(
                "테스트 제목", "테스트 내용", "http://example.com/image.jpg"
        );

        given(memberRepository.findByEmail("author@example.com")).willReturn(Optional.of(author));
        given(postRepository.save(any(Post.class))).willReturn(testPost);

        // when
        Post result = postService.createPost(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("테스트 제목");
        verify(postRepository).save(any(Post.class));
    }

    @Test
    @DisplayName("존재하지 않는 회원이 게시글 작성을 시도하면 예외가 발생한다")
    void createPost_회원없음_예외() {
        // given
        setupSecurityContext("notfound@example.com", Role.USER);
        PostCreateRequestDto request = new PostCreateRequestDto(
                "제목", "내용", "img.jpg"
        );

        given(memberRepository.findByEmail("notfound@example.com")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.createPost(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("페이지네이션으로 전체 게시글 목록을 조회할 수 있다")
    void getAllPosts_성공() {
        // given
        PageImpl<Post> page = new PageImpl<>(List.of(testPost), PageRequest.of(0, 10), 1);
        given(postRepository.findAll(any(PageRequest.class))).willReturn(page);

        // when
        Page<PostResponseDto> result = postService.getAllPosts(PageRequest.of(0, 10));

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).title()).isEqualTo("테스트 제목");
        assertThat(result.getContent().get(0).email()).isEqualTo("author@example.com");
    }

    @Test
    @DisplayName("No-Offset 커서 기반으로 게시글 목록을 Slice 조회할 수 있다")
    void getPostsByCursor_성공() {
        // given
        Slice<Post> slice = new SliceImpl<>(List.of(testPost), PageRequest.of(0, 10), false);
        given(postRepository.findPostsByCursor(any(), any(PageRequest.class))).willReturn(slice);

        // when
        Slice<PostResponseDto> result = postService.getPostsByCursor(10L, 10);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).title()).isEqualTo("테스트 제목");
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    @DisplayName("ID로 단일 게시글을 조회할 수 있다")
    void getPost_성공() {
        // given
        given(postRepository.findById(10L)).willReturn(Optional.of(testPost));

        // when
        PostResponseDto result = postService.getPost(10L);

        // then
        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.title()).isEqualTo("테스트 제목");
    }

    @Test
    @DisplayName("존재하지 않는 게시글 ID로 조회하면 예외가 발생한다")
    void getPost_없음_예외() {
        // given
        given(postRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.getPost(999L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.POST_NOT_FOUND);
    }

    @Test
    @DisplayName("작성자 본인이 게시글을 수정할 수 있다")
    void updatePost_작성자_성공() {
        // given
        setupSecurityContext("author@example.com", Role.USER);
        PostUpdateRequestDto updateDto = new PostUpdateRequestDto("수정 제목", "new.jpg", "수정 내용");

        given(postRepository.findById(10L)).willReturn(Optional.of(testPost));

        // when
        PostResponseDto result = postService.updatePost(10L, updateDto);

        // then
        assertThat(result.title()).isEqualTo("수정 제목");
        assertThat(result.content()).isEqualTo("수정 내용");
    }

    @Test
    @DisplayName("관리자(ADMIN) 권한으로 타인의 게시글을 수정할 수 있다")
    void updatePost_관리자_성공() {
        // given
        setupSecurityContext("admin@example.com", Role.ADMIN);
        PostUpdateRequestDto updateDto = new PostUpdateRequestDto("관리자 수정", "admin.jpg", "관리자 내용");

        given(postRepository.findById(10L)).willReturn(Optional.of(testPost));

        // when
        PostResponseDto result = postService.updatePost(10L, updateDto);

        // then
        assertThat(result.title()).isEqualTo("관리자 수정");
    }

    @Test
    @DisplayName("작성자도 아니고 관리자도 아닌 경우 게시글 수정 시 예외가 발생한다")
    void updatePost_권한없음_예외() {
        // given
        setupSecurityContext("other@example.com", Role.USER);
        PostUpdateRequestDto updateDto = new PostUpdateRequestDto("수정 시도", "new.jpg", "수정 내용");

        given(postRepository.findById(10L)).willReturn(Optional.of(testPost));

        // when & then
        assertThatThrownBy(() -> postService.updatePost(10L, updateDto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED_AUTHOR);
    }

    @Test
    @DisplayName("작성자 본인이 게시글을 삭제할 수 있다")
    void deletePost_작성자_성공() {
        // given
        setupSecurityContext("author@example.com", Role.USER);
        given(postRepository.findById(10L)).willReturn(Optional.of(testPost));

        // when
        postService.deletePost(10L);

        // then
        verify(postRepository).delete(testPost);
    }

    @Test
    @DisplayName("권한이 없는 사용자가 삭제 시도하면 예외가 발생한다")
    void deletePost_권한없음_예외() {
        // given
        setupSecurityContext("other@example.com", Role.USER);
        given(postRepository.findById(10L)).willReturn(Optional.of(testPost));

        // when & then
        assertThatThrownBy(() -> postService.deletePost(10L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED_AUTHOR);
    }
}
