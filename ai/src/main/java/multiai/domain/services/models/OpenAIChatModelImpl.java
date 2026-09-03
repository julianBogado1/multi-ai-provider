package multiai.domain.services.models;

import multiai.details.web.AICompletionProvider;
import multiai.details.web.AICompletionProviderImpl;
import multiai.domain.interfaces.models.OpenAIChatModel;
import multiai.domain.models.CompletionRequest;
import multiai.domain.models.CompletionResponse;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;

public class OpenAIChatModelImpl implements OpenAIChatModel {
    private final AICompletionProvider aiProvider;

    public OpenAIChatModelImpl() {
        this(new AICompletionProviderImpl());
    }

    public OpenAIChatModelImpl(AICompletionProvider aiProvider) {
        this.aiProvider = aiProvider;
    }

    
    @Override
    public ChatResponse call(Prompt prompt) {
        final CompletionRequest completionRequest = new CompletionRequest(prompt.getContents());
        final CompletionResponse completion = aiProvider.complete(completionRequest);
        return new ChatResponse(List.of(new Generation(new AssistantMessage(completion.completion()))));
    }
}
