package com.kindtail.adoptmate.comment.repository;

import com.kindtail.adoptmate.comment.domain.Comment;
import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.domain.Role;
import com.kindtail.adoptmate.member.repository.MemberRepository;
import com.kindtail.adoptmate.post.domain.Post;
import com.kindtail.adoptmate.post.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PostRepository postRepository;

    private Member member;
    private Post post;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .email("user@example.com")
                .name("사용자")
                .role(Role.USER)
                .build();
        member = memberRepository.save(member);

        post = Post.builder()
                .title("게시글 제목")
                .content("게시글 내용")
                .member(member)
                .build();
        post = postRepository.save(post);
    }

    @Test
    @DisplayName("findByPost 로 특정 게시글의 모든 댓글을 조회할 수 있다")
    void findByPost() {
        // given
        Comment comment1 = Comment.builder()
                .content("댓글 1")
                .member(member)
                .post(post)
                .build();

        Comment comment2 = Comment.builder()
                .content("댓글 2")
                .member(member)
                .post(post)
                .build();

        commentRepository.save(comment1);
        commentRepository.save(comment2);

        // when
        List<Comment> comments = commentRepository.findByPost(post);

        // then
        assertThat(comments).hasSize(2);
        assertThat(comments.get(0).getMember().getName()).isEqualTo("사용자");
    }

    @Test
    @DisplayName("findByPostAndParentIsNull 로 부모가 없는 최상위(루트) 댓글만 조회할 수 있다")
    void findByPostAndParentIsNull() {
        // given
        Comment parentComment = Comment.builder()
                .content("루트 댓글")
                .member(member)
                .post(post)
                .build();
        Comment savedParent = commentRepository.save(parentComment);

        Comment childComment = Comment.builder()
                .content("대댓글")
                .parent(savedParent)
                .member(member)
                .post(post)
                .build();
        commentRepository.save(childComment);

        // when
        List<Comment> rootComments = commentRepository.findByPostAndParentIsNull(post);

        // then
        assertThat(rootComments).hasSize(1);
        assertThat(rootComments.get(0).getContent()).isEqualTo("루트 댓글");
    }

    @Test
    @DisplayName("댓글을 삭제할 수 있다")
    void deleteComment() {
        // given
        Comment comment = Comment.builder()
                .content("삭제할 댓글")
                .member(member)
                .post(post)
                .build();
        Comment savedComment = commentRepository.save(comment);

        // when
        commentRepository.delete(savedComment);

        // then
        assertThat(commentRepository.findById(savedComment.getId())).isEmpty();
    }
}
