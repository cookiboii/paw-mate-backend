package com.kindtail.adoptmate.adoption.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class AdoptionStatusTest {

    @Test
    @DisplayName("AdoptionStatus 는 PENDING, APPROVED, REJECTED 상태를 가진다")
    void adoptionStatusValues() {
        // given & when
        AdoptionStatus[] statuses = AdoptionStatus.values();

        // then
        assertThat(statuses).hasSize(3);
        assertThat(statuses).containsExactly(AdoptionStatus.PENDING, AdoptionStatus.APPROVED, AdoptionStatus.REJECTED);
    }
}
