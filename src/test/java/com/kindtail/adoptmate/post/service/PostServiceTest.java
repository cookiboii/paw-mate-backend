package com.kindtail.adoptmate.post.service;

import com.kindtail.adoptmate.auth.CustomUserDetails;
import com.kindtail.adoptmate.auth.CurrentUserProvider;
import com.kindtail.adoptmate.common.exception.CustomException;
import com.kindtail.adoptmate.common.exception.ErrorCode;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.domain.Role;
import com.kindtail.adoptmate.member.repository.MemberRepository;
import com.kindtail.adoptmate.post.domain.Post;
import com.kindtail.adoptmate.post.dto.PostCreateRequest;
import com.kindtail.adoptmate.post.dto.PostResponse;
import com.kindtail.adoptmate.post.dto.PostUpdateRequest;
import com.kindtail.adoptmate.post.repository.PostRepository;
import com.kindtail.adoptmate.post.repository.PostLikeRepository;
import com.kindtail.adoptmate.post.repository.PostBookmarkRepository;
import com.kindtail.adoptmate.comment.repository.CommentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

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

    @Mock private PostLikeRepository postLikeRepository;
    @Mock private PostBookmarkRepository postBookmarkRepository;
    @Mock private CommentRepository commentRepository;

    @Spy
    private CurrentUserProvider currentUserProvider = new CurrentUserProvider();

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
        Long memberId = switch (email) {
            case "other@example.com" -> 2L;
            case "admin@example.com" -> 99L;
            default -> 1L;
        };
        Member member = Member.builder()
            .id(memberId)
            .email(email)
            .name("테스트유저")
            .role(role)
            .build();
        CustomUserDetails userDetails = new CustomUserDetails(member);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            userDetails, null, List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("로그인한 회원이 게시글을 성공적으로 등록할 수 있다")
    void createPost_성공() {
        // given
        setupSecurityContext("author@example.com", Role.USER);
        PostCreateRequest request = new PostCreateRequest(
            "테스트 제목", "테스트 내용", "http://example.com/image.jpg"
        );

        given(memberRepository.findByEmail("author@example.com")).willReturn(Optional.of(author));
        given(postRepository.save(any(Post.class))).willReturn(testPost);

        // when
        PostResponse result = postService.createPost(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("테스트 제목");
        verify(postRepository).save(any(Post.class));
    }

    @Test
    @DisplayName("존재하지 않는 회원이 게시글 작성을 시도하면 예외가 발생한다")
    void createPost_회원없음_예외() {
        // given
        setupSecurityContext("notfound@example.com", Role.USER);
        PostCreateRequest request = new PostCreateRequest(
            "제목", "내용", "img.jpg"
        );

        given(memberRepository.findByEmail("notfound@example.com")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.createPost(request))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    void likePostIsIdempotentWhenAlreadyLiked() {
        setupSecurityContext("author@example.com", Role.USER);
        given(postRepository.findByIdForUpdate(10L)).willReturn(Optional.of(testPost));
        given(postLikeRepository.findByPostIdAndMemberId(10L, 1L))
                .willReturn(Optional.of(com.kindtail.adoptmate.post.domain.PostLike.builder()
                        .post(testPost).member(author).build()));
        given(postLikeRepository.countByPostId(10L)).willReturn(1L);

        var response = postService.likePost(10L);

        assertThat(response.liked()).isTrue();
        assertThat(response.likeCount()).isEqualTo(1L);
        verify(postRepository).findByIdForUpdate(10L);
    }

    @Test
    void bookmarkPostIsIdempotentWhenAlreadyBookmarked() {
        setupSecurityContext("author@example.com", Role.USER);
        given(postRepository.findByIdForUpdate(10L)).willReturn(Optional.of(testPost));
        given(postBookmarkRepository.findByPostIdAndMemberId(10L, 1L))
                .willReturn(Optional.of(com.kindtail.adoptmate.post.domain.PostBookmark.builder()
                        .post(testPost).member(author).build()));

        var response = postService.bookmarkPost(10L);

        assertThat(response.bookmarked()).isTrue();
        verify(postRepository).findByIdForUpdate(10L);
    }

    @Test
    @DisplayName("작성자 본인이 게시글을 수정할 수 있다")
    void updatePost_작성자_성공() {
        // given
        setupSecurityContext("author@example.com", Role.USER);
        PostUpdateRequest request = new PostUpdateRequest("수정 제목", "new.jpg", "수정 내용");

        given(postRepository.findById(10L)).willReturn(Optional.of(testPost));

        // when
        PostResponse result = postService.updatePost(10L, request);

        // then
        assertThat(result.title()).isEqualTo("수정 제목");
        assertThat(result.content()).isEqualTo("수정 내용");
    }

    @Test
    @DisplayName("관리자(ADMIN)라도 타인의 게시글을 수정할 수 없다")
    void updatePost_관리자_예외() {
        // given
        setupSecurityContext("admin@example.com", Role.ADMIN);
        PostUpdateRequest request = new PostUpdateRequest("관리자 수정", "admin.jpg", "관리자 내용");

        given(postRepository.findById(10L)).willReturn(Optional.of(testPost));

        // when & then
        assertThatThrownBy(() -> postService.updatePost(10L, request))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.POST_AUTHOR_REQUIRED);
    }

    @Test
    @DisplayName("작성자도 아니고 관리자도 아닌 경우 게시글 수정 시 예외가 발생한다")
    void updatePost_권한없음_예외() {
        // given
        setupSecurityContext("other@example.com", Role.USER);
        PostUpdateRequest request = new PostUpdateRequest("수정 시도", "new.jpg", "수정 내용");

        given(postRepository.findById(10L)).willReturn(Optional.of(testPost));

        // when & then
        assertThatThrownBy(() -> postService.updatePost(10L, request))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.POST_AUTHOR_REQUIRED);
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
    @DisplayName("관리자는 타인의 게시글을 삭제할 수 있다")
    void deletePost_관리자_성공() {
        // given
        setupSecurityContext("admin@example.com", Role.ADMIN);
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
