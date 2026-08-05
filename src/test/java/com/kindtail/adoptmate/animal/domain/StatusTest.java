package com.kindtail.adoptmate.animal.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StatusTest {

    @Test
    void WAITING_PROTECTED_ADOPTED_값이_존재한다() {
        // given & when
        Status waiting = Status.WAITING;
        Status protectedStatus = Status.PROTECTED;
        Status adopted = Status.ADOPTED;

        // then
        assertThat(waiting).isNotNull();
        assertThat(protectedStatus).isNotNull();
        assertThat(adopted).isNotNull();
        assertThat(waiting.name()).isEqualTo("WAITING");
        assertThat(protectedStatus.name()).isEqualTo("PROTECTED");
        assertThat(adopted.name()).isEqualTo("ADOPTED");
    }

    @Test
    void valueOf_로_Enum_값을_가져올_수_있다() {
        // given
        String waitingString = "WAITING";
        String protectedString = "PROTECTED";
        String adoptedString = "ADOPTED";

        // when
        Status waiting = Status.valueOf(waitingString);
        Status protectedStatus = Status.valueOf(protectedString);
        Status adopted = Status.valueOf(adoptedString);

        // then
        assertThat(waiting).isEqualTo(Status.WAITING);
        assertThat(protectedStatus).isEqualTo(Status.PROTECTED);
        assertThat(adopted).isEqualTo(Status.ADOPTED);
    }
}
