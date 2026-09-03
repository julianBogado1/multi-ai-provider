package multiai.domain;

import java.util.List;

import multiai.details.web.AICompletionProvider;
import multiai.domain.models.CompletionRequest;
import multiai.details.web.AICompletionProviderImpl;
import multiai.domain.models.CompletionResponse;
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
 * against the mock exactly as it would against a real provider.
 */
public class MockAiChatModel implements ChatModel {

	private final AICompletionProvider aiProvider;

	public MockAiChatModel() {
		this(new AICompletionProviderImpl());
	}

	public MockAiChatModel(final AICompletionProvider aiCompletionProviderImpl) {
	    this.aiProvider = aiCompletionProviderImpl;
    }

	@Override
	public ChatResponse call(final Prompt prompt) {
        final CompletionRequest completionRequest = new CompletionRequest(prompt.getContents());
		final CompletionResponse completion = aiProvider.complete(completionRequest);
		return new ChatResponse(List.of(new Generation(new AssistantMessage(completion.completion()))));
	}
}
