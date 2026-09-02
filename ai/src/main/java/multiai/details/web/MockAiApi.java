package multiai.details.web;

import java.net.http.HttpClient;

import multiai.domain.models.CompletionRequest;
import multiai.domain.models.CompletionResponse;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Thin HTTP client for the completions mock API: {@code POST /completions}
 * with the prompt in the body, answered with {@code {"completion": "..."}}.
 */
public class MockAiApi implements MockAiCompletionApi{

	private final RestClient restClient;

	public MockAiApi(final String baseUrl) {
		this(RestClient.builder()
			.baseUrl(baseUrl)
			.requestFactory(new JdkClientHttpRequestFactory(
					HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build()))
			.build());
	}

	public MockAiApi(final RestClient restClient) {
		this.restClient = restClient;
	}

	public CompletionResponse complete(CompletionRequest completionRequest) {
		return this.restClient.post()
			.uri("/completions")
			.contentType(MediaType.APPLICATION_JSON)
			.body(new CompletionRequest(completionRequest.prompt()))
			.retrieve()
			.body(CompletionResponse.class);
	}
}
