package multiai.boot;

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
		SpringApplication.run(Application.class, args);
	}
}
