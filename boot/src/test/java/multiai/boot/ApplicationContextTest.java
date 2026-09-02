package multiai.boot;

import static org.assertj.core.api.Assertions.assertThat;

import multiai.details.web.controllers.CompletionController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
class ApplicationContextTest {

	@Autowired
	private ApplicationContext context;

	@Test
	void context_loads_with_the_completion_controller() {
		assertThat(this.context.getBean(CompletionController.class)).isNotNull();
	}
}
