package multiai.details.web;

import java.net.http.HttpClient;

import multiai.domain.models.CompletionRequest;
import multiai.domain.models.CompletionResponse;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Thin HTTP client for the completions mock API: {@code POST /completions?schema=true}
 * with a JSON Schema as the body, answered with a document that complies with it.
 *
 * <p>
 * Only structured output is supported, so the prompt carried by the
 * {@link CompletionRequest} is always the schema itself.
 */
public class AICompletionProviderImpl implements AICompletionProvider {

	public static final String PRODUCTION_BASE_URL = "https://competions-mock-api.vercel.app";

	private final RestClient restClient;

	public AICompletionProviderImpl() {
		this(PRODUCTION_BASE_URL);
	}

	public AICompletionProviderImpl(final String baseUrl) {
		this(RestClient.builder()
			.baseUrl(baseUrl)
			.requestFactory(new JdkClientHttpRequestFactory(
					HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build()))
			.build());
	}

	public AICompletionProviderImpl(final RestClient restClient) {
		this.restClient = restClient;
	}

	@Override
	public CompletionResponse complete(final CompletionRequest completionRequest) {
		final String document = this.restClient.post()
			.uri(uriBuilder -> uriBuilder.path("/completions").queryParam("schema", true).build())
			.contentType(MediaType.APPLICATION_JSON)
			.body(completionRequest.prompt())
			.retrieve()
			.body(String.class);
		return new CompletionResponse(document);
	}
}
