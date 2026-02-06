package gov.nist.emp.bankcard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class BankcardApplication {
	private static final Logger log = LoggerFactory.getLogger(BankcardApplication.class);

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(BankcardApplication.class);
		app.addListeners((ApplicationListener<ApplicationReadyEvent>) event -> {
			Environment env = event.getApplicationContext().getEnvironment();
			String appEnv = env.getProperty("app.environment", "not set");
			String version = env.getProperty("version", "not set");
			log.info("\n\n==============================\n  app.environment: {}\n  version: {}\n==============================\n", appEnv, version);
		});
		app.run(args);
	}
}
