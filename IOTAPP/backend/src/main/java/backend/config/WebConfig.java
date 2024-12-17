package backend.config;

import java.time.Duration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class WebConfig {

	private static final long CONNECT_TIMEOUT_MILLIS = 3000;
	private static final long READ_TIMEOUT_MILLIS = 3000;

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder
				.setConnectTimeout(Duration.ofMillis(CONNECT_TIMEOUT_MILLIS))
				.setReadTimeout(Duration.ofMillis(READ_TIMEOUT_MILLIS))
				.build();
	}
}