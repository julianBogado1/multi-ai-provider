package multiai.domain.services.orchestration;

import multiai.domain.interfaces.models.DomainChatModel;
import multiai.domain.interfaces.orchestration.ChatModelOrchestrator;
import multiai.domain.services.models.AWSBedrockChatModelImpl;
import multiai.domain.services.models.AnthropicChatModelImpl;
import multiai.domain.services.models.OLlamaChatModelImpl;
import multiai.domain.services.models.OpenAIChatModelImpl;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ChatModelOrchestratorImpl implements ChatModelOrchestrator {

    private final Map<String, DomainChatModel> models = new LinkedHashMap<>();

    public ChatModelOrchestratorImpl() {
        models.put("OpenAI", new OpenAIChatModelImpl());
        models.put("Anthropic", new AnthropicChatModelImpl());
        models.put("AWSBedrock", new AWSBedrockChatModelImpl());
        models.put("OLlama", new OLlamaChatModelImpl());
    }

    @Override
    public DomainChatModel getChatModel(String chatModel) {
        return models.get(chatModel);
    }

    @Override
    public Set<String> getSupportedChatModels() {
        return models.keySet();
    }
}
