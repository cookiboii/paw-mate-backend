package com.kindtail.adoptmate.member.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoleTest {

    @Test
    void USER_및_ADMIN_값이_존재한다 () {
        // given & when
        Role user = Role.USER;
        Role admin = Role.ADMIN;

        // then
        assertThat(user).isNotNull();
        assertThat(admin).isNotNull();
        assertThat(user.name()).isEqualTo("USER");
        assertThat(admin.name()).isEqualTo("ADMIN");
    }

    @Test
    void valueOf_로_Enum_값을_가져올_수_있다 () {
        // given
        String userString = "USER";
        String adminString = "ADMIN";

        // when
        Role user = Role.valueOf(userString);
        Role admin = Role.valueOf(adminString);

        // then
        assertThat(user).isEqualTo(Role.USER);
        assertThat(admin).isEqualTo(Role.ADMIN);
    }
}
