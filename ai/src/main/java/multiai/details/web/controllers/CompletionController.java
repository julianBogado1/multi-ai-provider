package multiai.details.web.controllers;

import multiai.domain.MockAiChatModel;
import multiai.domain.interfaces.orchestration.ChatModelOrchestrator;
import multiai.domain.models.HelpResponse;
import multiai.domain.services.orchestration.ChatModelOrchestratorImpl;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class CompletionController {

	private static final String DEFAULT_MODEL = "mock";

	private static final String USAGE = "POST /completions?schema=true&model=<model> with a JSON Schema as the "
		+ "body. Only structured output is supported.";

	private final ChatModelOrchestrator orchestrator = new ChatModelOrchestratorImpl();
	private final ChatModel defaultChatModel = new MockAiChatModel();

	@GetMapping("/help")
	public HelpResponse help() {
		return new HelpResponse(this.orchestrator.getSupportedChatModels(), DEFAULT_MODEL, USAGE);
	}

	@PostMapping(path = "/completions", params = "schema=true", produces = MediaType.APPLICATION_JSON_VALUE)
	public String complete(
			@RequestBody final String jsonSchema,
			@RequestParam(name = "model", required = false) final String model) {
		final var chatModel = resolve(model);
		final var response = chatModel.call(new Prompt(jsonSchema));
		return response.getResult().getOutput().getText();
	}

	/**
	 * Reached when {@code schema=true} is absent, empty or false. Structured output is
	 * the only supported mode, so the request is rejected with the usage instructions.
	 */
	@PostMapping("/completions")
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public HelpResponse schemaRequired() {
		return help();
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
