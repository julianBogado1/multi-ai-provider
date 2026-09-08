package multiai.ai;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
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

	private static final String SCHEMA = "{\"type\": \"object\", \"properties\": {\"name\": {\"type\": \"string\"}}}";

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
	void call_posts_the_schema_and_returns_the_generated_document() {
		server.stubFor(post(urlEqualTo("/completions?schema=true")).willReturn(okJson("{\"name\": \"hola mundo\"}")));
		final var model = new MockAiChatModel(new MockAiApi(server.baseUrl()));

		final var response = model.call(new Prompt(SCHEMA));

		assertThat(response.getResult().getOutput().getText()).isEqualTo("{\"name\": \"hola mundo\"}");
		server.verify(postRequestedFor(urlEqualTo("/completions?schema=true")).withRequestBody(equalToJson(SCHEMA)));
	}
}
