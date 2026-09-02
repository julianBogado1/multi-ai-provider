package multiai.details.web.controllers;

import multiai.domain.MockAiChatModel;
import multiai.domain.models.CompletionRequest;
import multiai.domain.models.CompletionResponse;
import multiai.details.web.MockAiApi;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CompletionController {

    private final MockAiApi api = new MockAiApi("https://competions-mock-api.vercel.app");
	private final ChatModel chatModel = new MockAiChatModel(api);

	@PostMapping("/completions")
	public CompletionResponse complete(@RequestBody final CompletionRequest request) {
		final var response = this.chatModel.call(new Prompt(request.prompt()));
		return new CompletionResponse(response.getResult().getOutput().getText());
	}
}
