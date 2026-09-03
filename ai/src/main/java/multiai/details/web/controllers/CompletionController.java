package multiai.details.web.controllers;

import multiai.domain.MockAiChatModel;
import multiai.domain.interfaces.orchestration.ChatModelOrchestrator;
import multiai.domain.models.CompletionRequest;
import multiai.domain.models.CompletionResponse;
import multiai.domain.models.HelpResponse;
import multiai.domain.services.orchestration.ChatModelOrchestratorImpl;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class CompletionController {

	private static final String DEFAULT_MODEL = "mock";

	private final ChatModelOrchestrator orchestrator = new ChatModelOrchestratorImpl();
	private final ChatModel defaultChatModel = new MockAiChatModel();

	@GetMapping("/help")
	public HelpResponse help() {
		return new HelpResponse(
			this.orchestrator.getSupportedChatModels(),
			DEFAULT_MODEL,
			"POST /completions?model=<model> with body {\"prompt\": \"...\"}");
	}

	@PostMapping("/completions")
	public CompletionResponse complete(
			@RequestBody final CompletionRequest request,
			@RequestParam(name = "model", required = false) final String model) {
		final var chatModel = resolve(model);
		final var response = chatModel.call(new Prompt(request.prompt()));
		return new CompletionResponse(response.getResult().getOutput().getText());
	}

	private ChatModel resolve(final String model) {
		if (model == null || model.isBlank() || DEFAULT_MODEL.equals(model)) {
			return this.defaultChatModel;
		}
		final var chatModel = this.orchestrator.getChatModel(model);
		if (chatModel == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
				"Unknown model '" + model + "'. Supported: " + this.orchestrator.getSupportedChatModels());
		}
		return chatModel;
	}
}
