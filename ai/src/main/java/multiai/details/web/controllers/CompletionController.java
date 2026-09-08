package multiai.details.web.controllers;

import java.util.Set;

import multiai.details.web.MockAiApi;
import multiai.domain.MockAiChatModel;
import multiai.domain.models.HelpResponse;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CompletionController {

	private static final String MODEL = "mock";

	private static final String USAGE = "POST /completions?schema=true with a JSON Schema as the body. "
		+ "Only structured output is supported.";

	private final ChatModel chatModel = new MockAiChatModel(new MockAiApi());

	@GetMapping("/help")
	public HelpResponse help() {
		return new HelpResponse(Set.of(MODEL), MODEL, USAGE);
	}

	@PostMapping(path = "/completions", params = "schema=true", produces = MediaType.APPLICATION_JSON_VALUE)
	public String complete(@RequestBody final String jsonSchema) {
		final var response = this.chatModel.call(new Prompt(jsonSchema));
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
}
