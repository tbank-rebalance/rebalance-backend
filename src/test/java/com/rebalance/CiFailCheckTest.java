package com.rebalance;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CiFailCheckTest {
    @Test
    void shouldFailOnPurpose() {
        assertEquals(1, 2, "Тест специально падает для проверки CI");
    }
}
