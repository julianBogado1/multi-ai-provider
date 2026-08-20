package multiai.ai;

import static org.assertj.core.api.Assertions.assertThat;

import multiai.infrastructure.web.MockAiApi;
import multiai.domain.MockAiAutoConfiguration;
import multiai.domain.MockAiChatModel;
import multiai.domain.MockAiProperties;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

class MockAiAutoConfigurationTest {

	private final ApplicationContextRunner runner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(MockAiAutoConfiguration.class));

	@Test
	void populates_the_chat_model_and_chat_client_beans() {
		this.runner.run(context -> {
			assertThat(context).hasSingleBean(MockAiApi.class);
			assertThat(context).hasSingleBean(ChatModel.class);
			assertThat(context).getBean(ChatModel.class).isInstanceOf(MockAiChatModel.class);
			assertThat(context).hasSingleBean(ChatClient.class);
		});
	}

	@Test
	void binds_the_base_url_property() {
		this.runner.withPropertyValues("ai.mock.base-url=http://localhost:9999")
			.run(context -> assertThat(context.getBean(MockAiProperties.class).baseUrl())
				.isEqualTo("http://localhost:9999"));
	}

	@Test
	void a_user_declared_chat_model_wins_over_the_mock() {
		this.runner.withUserConfiguration(CustomChatModelConfiguration.class).run(context -> {
			assertThat(context).hasSingleBean(ChatModel.class);
			assertThat(context).doesNotHaveBean(MockAiChatModel.class);
		});
	}

	@Configuration(proxyBeanMethods = false)
	static class CustomChatModelConfiguration {

		@Bean
		ChatModel customChatModel() {
			return prompt -> null;
		}
	}
}
