package com.kindtail.adoptmate.animal.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GenderTest {

    @Test
    void MALE_및_FEMALE_값이_존재한다() {
        // given & when
        Gender male = Gender.MALE;
        Gender female = Gender.FEMALE;

        // then
        assertThat(male).isNotNull();
        assertThat(female).isNotNull();
        assertThat(male.name()).isEqualTo("MALE");
        assertThat(female.name()).isEqualTo("FEMALE");
    }

    @Test
    void valueOf_로_Enum_값을_가져올_수_있다() {
        // given
        String maleString = "MALE";
        String femaleString = "FEMALE";

        // when
        Gender male = Gender.valueOf(maleString);
        Gender female = Gender.valueOf(femaleString);

        // then
        assertThat(male).isEqualTo(Gender.MALE);
        assertThat(female).isEqualTo(Gender.FEMALE);
    }
}
