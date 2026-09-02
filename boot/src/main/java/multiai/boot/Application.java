package multiai.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "multiai")
public class Application {
	public static void main(final String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
