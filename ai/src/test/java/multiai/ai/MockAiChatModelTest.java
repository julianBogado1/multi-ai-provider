package multiai.ai;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import multiai.details.web.MockAiApi;
import multiai.domain.MockAiChatModel;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.prompt.Prompt;

class MockAiChatModelTest {

	private static final WireMockServer server = new WireMockServer(WireMockConfiguration.options().dynamicPort());

	@BeforeAll
	static void start() {
		server.start();
	}

	@AfterAll
	static void stop() {
		server.stop();
	}

	@BeforeEach
	void reset() {
		server.resetAll();
	}

	@Test
	void call_posts_the_prompt_and_maps_the_completion() {
		server.stubFor(post(urlEqualTo("/completions")).willReturn(okJson("{\"completion\": \"hola mundo\"}")));
		final var model = new MockAiChatModel(new MockAiApi(server.baseUrl()));

		final var response = model.call(new Prompt("decime hola"));

		assertThat(response.getResult().getOutput().getText()).isEqualTo("hola mundo");
		server.verify(postRequestedFor(urlEqualTo("/completions"))
			.withRequestBody(matchingJsonPath("$.prompt", equalTo("decime hola"))));
	}
}
