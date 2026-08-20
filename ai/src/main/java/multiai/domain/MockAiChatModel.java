package multiai.domain;

import java.util.List;

import multiai.infrastructure.web.MockAiApi;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

/**
 * A Spring AI {@link ChatModel} backed by the completions mock API.
 *
 * <p>
 * Because it implements the vendor-neutral port, everything built on top of a
 * {@code ChatModel} — {@code ChatClient}, advisors, structured output — works
 * against the mock exactly as it would against a real provider, and swapping in
 * a real one is a bean definition, not a rewrite.
 */
public class MockAiChatModel implements ChatModel {

	private final MockAiApi api;

	public MockAiChatModel(final MockAiApi api) {
		this.api = api;
	}

	@Override
	public ChatResponse call(final Prompt prompt) {
        final var completionRequest = new CompletionRequest(prompt.getContents());
		final var completion = this.api.complete(completionRequest);
		return new ChatResponse(List.of(new Generation(new AssistantMessage(completion.completion()))));
	}
}
