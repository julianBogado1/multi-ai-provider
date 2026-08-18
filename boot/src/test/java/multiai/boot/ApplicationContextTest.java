package multiai.boot;

import static org.assertj.core.api.Assertions.assertThat;

import multiai.ai.MockAiChatModel;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
class ApplicationContextTest {

	@Autowired
	private ApplicationContext context;

	@Test
	void context_loads_with_the_auto_configured_ai_beans() {
		assertThat(this.context.getBean(ChatModel.class)).isInstanceOf(MockAiChatModel.class);
		assertThat(this.context.getBean(ChatClient.class)).isNotNull();
	}
}
