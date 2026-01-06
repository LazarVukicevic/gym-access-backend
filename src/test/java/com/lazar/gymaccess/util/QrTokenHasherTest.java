package com.lazar.gymaccess.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class QrTokenHasherTest {

    @Test
    void hash_isDeterministic_and64Chars() {
        String token = "test-token";

        String h1 = QrTokenHasher.hash(token);
        String h2 = QrTokenHasher.hash(token);

        assertThat(h1).isEqualTo(h2);
        assertThat(h1).hasSize(64);
    }
}
