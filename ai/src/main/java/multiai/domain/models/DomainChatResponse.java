package multiai.domain.models;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;

import java.util.List;

public class DomainChatResponse extends ChatResponse {

    public DomainChatResponse(List<Generation> generations) {
        super(generations);
    }
}
