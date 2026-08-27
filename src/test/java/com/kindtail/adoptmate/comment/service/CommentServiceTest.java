package com.kindtail.adoptmate.comment.service;

import com.kindtail.adoptmate.auth.TokenUserInfo;
import com.kindtail.adoptmate.comment.domain.Comment;
import com.kindtail.adoptmate.comment.dto.CommentDto;
import com.kindtail.adoptmate.comment.dto.CommentResponseDto;
import com.kindtail.adoptmate.comment.dto.CommentUpdateDto;
import com.kindtail.adoptmate.comment.repository.CommentRepository;
import com.kindtail.adoptmate.common.exception.CustomException;
import com.kindtail.adoptmate.common.exception.ErrorCode;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.domain.Role;
import com.kindtail.adoptmate.member.repository.MemberRepository;
import com.kindtail.adoptmate.post.domain.Post;
import com.kindtail.adoptmate.post.repository.PostRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private CommentService commentService;

    private Member author;
    private Post testPost;
    private Comment parentComment;

    @BeforeEach
    void setUp() {
        author = Member.builder()
                .id(1L)
                .email("commenter@example.com")
                .name("댓글작성자")
                .role(Role.USER)
                .build();

        testPost = Post.builder()
                .id(10L)
                .title("게시글 제목")
                .content("게시글 내용")
                .member(author)
                .build();

        parentComment = Comment.builder()
                .id(100L)
                .content("부모 댓글")
                .member(author)
                .post(testPost)
                .children(new ArrayList<>())
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
    @DisplayName("루트 댓글을 성공적으로 등록할 수 있다")
    void addRootComment_성공() {
        // given
        setupSecurityContext("commenter@example.com", Role.USER);
        CommentDto commentDto = new CommentDto(null, "새로운 루트 댓글");

        given(memberRepository.findByEmail("commenter@example.com")).willReturn(Optional.of(author));
        given(postRepository.findById(10L)).willReturn(Optional.of(testPost));
        given(commentRepository.save(any(Comment.class))).willReturn(parentComment);

        // when
        CommentResponseDto result = commentService.addComment(10L, commentDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.authorEmail()).isEqualTo("commenter@example.com");
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    @DisplayName("대댓글(parent가 존재하는 댓글)을 성공적으로 등록할 수 있다")
    void addChildComment_성공() {
        // given
        setupSecurityContext("commenter@example.com", Role.USER);
        CommentDto commentDto = new CommentDto(100L, "대댓글 내용");

        given(memberRepository.findByEmail("commenter@example.com")).willReturn(Optional.of(author));
        given(postRepository.findById(10L)).willReturn(Optional.of(testPost));
        given(commentRepository.findById(100L)).willReturn(Optional.of(parentComment));

        Comment childComment = Comment.builder()
                .id(101L)
                .content("대댓글 내용")
                .parent(parentComment)
                .member(author)
                .post(testPost)
                .children(new ArrayList<>())
                .build();

        given(commentRepository.save(any(Comment.class))).willReturn(childComment);

        // when
        CommentResponseDto result = commentService.addComment(10L, commentDto);

        // then
        assertThat(result).isNotNull();
        verify(commentRepository).findById(100L);
    }

    @Test
    @DisplayName("존재하지 않는 부모 댓글 ID를 지정하면 예외가 발생한다")
    void addComment_부모댓글없음_예외() {
        // given
        setupSecurityContext("commenter@example.com", Role.USER);
        CommentDto commentDto = new CommentDto(999L, "잘못된 대댓글");

        given(memberRepository.findByEmail("commenter@example.com")).willReturn(Optional.of(author));
        given(postRepository.findById(10L)).willReturn(Optional.of(testPost));
        given(commentRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.addComment(10L, commentDto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_NOT_FOUND);
    }

    @Test
    @DisplayName("게시글의 최상위 댓글 및 대댓글 목록을 조회할 수 있다")
    void getComments_성공() {
        // given
        given(postRepository.findById(10L)).willReturn(Optional.of(testPost));
        given(commentRepository.findByPostAndParentIsNull(testPost)).willReturn(List.of(parentComment));

        // when
        List<CommentResponseDto> result = commentService.getComments(10L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).content()).isEqualTo("부모 댓글");
    }

    @Test
    @DisplayName("작성자 본인이 댓글을 수정할 수 있다")
    void updateComment_작성자_성공() {
        // given
        setupSecurityContext("commenter@example.com", Role.USER);
        CommentUpdateDto updateDto = new CommentUpdateDto(100L, "수정된 댓글");

        given(commentRepository.findById(100L)).willReturn(Optional.of(parentComment));

        // when
        CommentResponseDto result = commentService.updateComment(100L, updateDto);

        // then
        assertThat(result.content()).isEqualTo("수정된 댓글");
    }

    @Test
    @DisplayName("타인의 댓글을 수정하려고 하면 예외가 발생한다")
    void updateComment_권한없음_예외() {
        // given
        setupSecurityContext("other@example.com", Role.USER);
        CommentUpdateDto updateDto = new CommentUpdateDto(100L, "수정 시도");

        given(commentRepository.findById(100L)).willReturn(Optional.of(parentComment));

        // when & then
        assertThatThrownBy(() -> commentService.updateComment(100L, updateDto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED_AUTHOR);
    }

    @Test
    @DisplayName("작성자 본인이 댓글을 삭제할 수 있다")
    void deleteComment_작성자_성공() {
        // given
        setupSecurityContext("commenter@example.com", Role.USER);
        given(commentRepository.findById(100L)).willReturn(Optional.of(parentComment));

        // when
        commentService.deleteComment(100L);

        // then
        verify(commentRepository).delete(parentComment);
    }

    @Test
    @DisplayName("관리자(ADMIN)가 타인의 댓글을 삭제할 수 있다")
    void deleteComment_관리자_성공() {
        // given
        setupSecurityContext("admin@example.com", Role.ADMIN);
        given(commentRepository.findById(100L)).willReturn(Optional.of(parentComment));

        // when
        commentService.deleteComment(100L);

        // then
        verify(commentRepository).delete(parentComment);
    }

    @Test
    @DisplayName("권한 없는 사용자가 댓글을 삭제하려고 하면 예외가 발생한다")
    void deleteComment_권한없음_예외() {
        // given
        setupSecurityContext("other@example.com", Role.USER);
        given(commentRepository.findById(100L)).willReturn(Optional.of(parentComment));

        // when & then
        assertThatThrownBy(() -> commentService.deleteComment(100L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED_AUTHOR);
    }
}
