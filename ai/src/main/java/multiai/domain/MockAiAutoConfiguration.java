package multiai.domain;

import multiai.infrastructure.web.MockAiApi;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Composition root of the AI module, contributed as a Boot auto-configuration.
 *
 * <p>
 * The module is a separate artifact with its own package root, so the consuming
 * application's component scan does not reach it — deliberately. Wiring arrives
 * through the {@code AutoConfiguration.imports} entry beside this class, so
 * depending on the jar is the whole integration.
 *
 * <p>
 * Every bean is {@link ConditionalOnMissingBean}, which is what makes the
 * defaults defaults: declare your own {@link ChatModel} and it wins, without
 * forking the module.
 */
@AutoConfiguration
@EnableConfigurationProperties(MockAiProperties.class)
public class MockAiAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public MockAiApi mockAiApi(final MockAiProperties properties) {
		return new MockAiApi(properties.baseUrl());
	}

	@Bean
	@ConditionalOnMissingBean(ChatModel.class)
	public MockAiChatModel mockAiChatModel(final MockAiApi api) {
		return new MockAiChatModel(api);
	}

	@Bean
	@ConditionalOnMissingBean
	public ChatClient chatClient(final ChatModel chatModel) {
		return ChatClient.create(chatModel);
	}
}
