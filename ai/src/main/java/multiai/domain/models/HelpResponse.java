package multiai.domain.models;

import java.util.Set;

public record HelpResponse(Set<String> supportedModels, String selectionStrategy, String usage) {
}
