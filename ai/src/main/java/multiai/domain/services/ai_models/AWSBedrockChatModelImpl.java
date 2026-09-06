package multiai.domain.services.ai_models;

import multiai.details.web.AICompletionProvider;
import multiai.details.web.AICompletionProviderImpl;
import multiai.domain.interfaces.models.AWSBedrockChatModel;
import multiai.domain.models.CompletionRequest;
import multiai.domain.models.CompletionResponse;
import multiai.domain.models.DomainChatResponse;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;

public class AWSBedrockChatModelImpl implements AWSBedrockChatModel {

    private final AICompletionProvider aiProvider;

    public AWSBedrockChatModelImpl() {
        this(new AICompletionProviderImpl());
    }

    public AWSBedrockChatModelImpl(final AICompletionProvider aiProvider) {
        this.aiProvider = aiProvider;
    }

    @Override
    public String name() {
        return "AWSBedrock";
    }

    @Override
    public DomainChatResponse call(final Prompt prompt) {
        final CompletionRequest completionRequest = new CompletionRequest(prompt.getContents());
        final CompletionResponse completion = this.aiProvider.complete(completionRequest);
        return new DomainChatResponse(List.of(new Generation(new AssistantMessage(completion.completion()))));
    }
}
