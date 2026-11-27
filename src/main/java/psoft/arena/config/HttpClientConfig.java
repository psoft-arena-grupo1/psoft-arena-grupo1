package psoft.arena.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class HttpClientConfig {

    @Bean
    RestClient.Builder restClientBuilder(
            @Value("${arena.llm.connect-timeout-millis:10000}") int connectMillis,
            @Value("${arena.llm.read-timeout-millis:30000}") int readMillis) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectMillis);
        factory.setReadTimeout(readMillis);
        return RestClient.builder().requestFactory(factory);
    }
}
