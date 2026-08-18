package multiai.ai;

import java.net.http.HttpClient;

import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Thin HTTP client for the completions mock API: {@code POST /completions}
 * with the prompt in the body, answered with {@code {"completion": "..."}}.
 */
public class MockAiApi {

	private final RestClient restClient;

	public MockAiApi(final String baseUrl) {
		// HTTP/1.1 pinned: the JDK client's default h2c upgrade is not spoken by
		// the plain-HTTP/1.1 servers this talks to (uvicorn, WireMock).
		this(RestClient.builder()
			.baseUrl(baseUrl)
			.requestFactory(new JdkClientHttpRequestFactory(
					HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build()))
			.build());
	}

	public MockAiApi(final RestClient restClient) {
		this.restClient = restClient;
	}

	public record CompletionRequest(String prompt) {
	}

	public record CompletionResponse(String completion) {
	}

	public CompletionResponse complete(final String prompt) {
		return this.restClient.post()
			.uri("/completions")
			.contentType(MediaType.APPLICATION_JSON)
			.body(new CompletionRequest(prompt))
			.retrieve()
			.body(CompletionResponse.class);
	}
}
