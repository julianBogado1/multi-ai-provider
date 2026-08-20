package multiai.infrastructure.web;

import multiai.domain.CompletionRequest;
import multiai.domain.CompletionResponse;

public interface MockAiCompletionApi {
    CompletionResponse complete(CompletionRequest completionRequest);
}
