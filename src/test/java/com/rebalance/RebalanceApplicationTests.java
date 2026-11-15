package com.rebalance;

import com.rebalance.service.auth.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class RebalanceApplicationTests {

	@MockitoBean
	private AuthService authService;

	@Test
	void contextLoads() {}
}
