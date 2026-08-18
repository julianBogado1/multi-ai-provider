package multiai.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuration for the mock AI provider.
 *
 * <p>
 * The default points at the completions-mock-api FastAPI app started with
 * {@code uvicorn main:app} in its own repository, which listens on port 8000.
 */
@ConfigurationProperties("ai.mock")
public record MockAiProperties(@DefaultValue("http://localhost:8000") String baseUrl) {
}
