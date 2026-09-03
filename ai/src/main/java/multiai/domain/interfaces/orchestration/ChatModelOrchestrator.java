package multiai.domain.interfaces.orchestration;

import java.util.Set;

import multiai.domain.interfaces.models.DomainChatModel;

public interface ChatModelOrchestrator {

    DomainChatModel getChatModel(String chatModel);

    Set<String> getSupportedChatModels();
}
