package multiai.domain.services.orchestration;

import multiai.domain.interfaces.models.DomainChatModel;
import multiai.domain.interfaces.orchestration.ChatModelOrchestrator;
import multiai.domain.services.ai_models.AWSBedrockChatModelImpl;
import multiai.domain.services.ai_models.AnthropicChatModelImpl;
import multiai.domain.services.ai_models.OLlamaChatModelImpl;
import multiai.domain.services.ai_models.OpenAIChatModelImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class ChatModelOrchestratorImpl implements ChatModelOrchestrator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatModelOrchestratorImpl.class);

    private final List<DomainChatModel> providers;

    public ChatModelOrchestratorImpl() {
        this(List.of(
            new OpenAIChatModelImpl(),
            new AnthropicChatModelImpl(),
            new AWSBedrockChatModelImpl(),
            new OLlamaChatModelImpl()));
    }

    public ChatModelOrchestratorImpl(final List<DomainChatModel> providers) {
        if (providers.isEmpty()) {
            throw new IllegalArgumentException("At least one chat model provider is required");
        }
        this.providers = List.copyOf(providers);
    }

    @Override
    public DomainChatModel getChatModel() {
        final DomainChatModel chosen = this.providers.get(ThreadLocalRandom.current().nextInt(this.providers.size()));
        IO.println("Load balancer selected chat model: " + chosen.name());
        return chosen;
    }

    @Override
    public Set<String> getSupportedChatModels() {
        return this.providers.stream()
            .map(DomainChatModel::name)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
