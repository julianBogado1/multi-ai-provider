package multiai.domain.interfaces.orchestration;

import java.util.Set;

import multiai.domain.interfaces.models.DomainChatModel;

public interface ChatModelOrchestrator {

    DomainChatModel getChatModel();

    Set<String> getSupportedChatModels();
}
