package multiai.boot;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The composition root. There is deliberately no widened {@code @ComponentScan}:
 * the ai module contributes its beans through its own auto-configuration, so
 * having it on the classpath is the whole integration.
 */
@SpringBootApplication
public class Application {
	public static void main(final String[] args) {
		final var context = SpringApplication.run(Application.class, args);
		final var chatClient = context.getBean(ChatClient.class);
		final var completion = chatClient.prompt().user("hola").call().content();
		System.out.println(completion);
	}
}
