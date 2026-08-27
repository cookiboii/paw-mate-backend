package com.kindtail.adoptmate.post.repository;

import com.kindtail.adoptmate.member.domain.Member;
import com.kindtail.adoptmate.member.domain.Role;
import com.kindtail.adoptmate.member.repository.MemberRepository;
import com.kindtail.adoptmate.post.domain.Post;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .email("writer@example.com")
                .name("작성자")
                .password("password123")
                .role(Role.USER)
                .build();
        memberRepository.save(testMember);
    }

    @Test
    @DisplayName("게시글을 저장하고 ID로 조회할 때 EntityGraph를 통해 Member 정보가 함께 조회된다")
    void saveAndFindByIdWithEntityGraph() {
        // given
        Post post = Post.builder()
                .title("테스트 제목")
                .content("테스트 내용")
                .image("test.jpg")
                .member(testMember)
                .build();
        Post savedPost = postRepository.save(post);

        // when
        Optional<Post> foundPost = postRepository.findById(savedPost.getId());

        // then
        assertThat(foundPost).isPresent();
        assertThat(foundPost.get().getTitle()).isEqualTo("테스트 제목");
        assertThat(foundPost.get().getMember().getEmail()).isEqualTo("writer@example.com");
    }

    @Test
    @DisplayName("페이지네이션으로 게시글 목록을 조회할 수 있다")
    void findAllWithPageable() {
        // given
        for (int i = 0; i < 5; i++) {
            Post post = Post.builder()
                    .title("제목 " + i)
                    .content("내용 " + i)
                    .member(testMember)
                    .build();
            postRepository.save(post);
        }

        // when
        Page<Post> postPage = postRepository.findAll(PageRequest.of(0, 3));

        // then
        assertThat(postPage.getTotalElements()).isEqualTo(5);
        assertThat(postPage.getContent()).hasSize(3);
    }

    @Test
    @DisplayName("게시글을 삭제할 수 있다")
    void deletePost() {
        // given
        Post post = Post.builder()
                .title("삭제할 게시글")
                .content("내용")
                .member(testMember)
                .build();
        Post savedPost = postRepository.save(post);

        // when
        postRepository.delete(savedPost);

        // then
        Optional<Post> foundPost = postRepository.findById(savedPost.getId());
        assertThat(foundPost).isEmpty();
    }
}
