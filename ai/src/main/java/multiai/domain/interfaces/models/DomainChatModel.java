package multiai.domain.interfaces.models;

import org.springframework.ai.chat.model.ChatModel;

public interface DomainChatModel extends ChatModel {

    String name();
}
