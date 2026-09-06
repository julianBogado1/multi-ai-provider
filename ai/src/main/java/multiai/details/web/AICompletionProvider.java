package multiai.details.web;

import multiai.domain.models.CompletionRequest;
import multiai.domain.models.CompletionResponse;

public interface AICompletionProvider {

    CompletionResponse complete(CompletionRequest completionRequest);
}
