package se.magnus.springcloud.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;

@SuppressWarnings("ALL")
@Configuration
public class HealthCheckConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(HealthCheckConfiguration.class);
    private final WebClient.Builder webClientBuilder;
    private HealthAggregator healthAggregator;
    private WebClient webClient;

    @Autowired
    public HealthCheckConfiguration(WebClient.Builder webClientBuilder, HealthAggregator healthAggregator) {
        this.webClientBuilder = webClientBuilder;
        this.healthAggregator = healthAggregator;
    }

    @Bean
    ReactiveHealthIndicator healthcheckMicroservices() {
        ReactiveHealthIndicatorRegistry registry = new DefaultReactiveHealthIndicatorRegistry(new LinkedHashMap<>());
        registry.register("auth-server", () -> getHealth("http://auth-server"));
        registry.register("player", () -> getHealth("http://player"));
        registry.register("team", () -> getHealth("http://team"));
        registry.register("nationality", () -> getHealth("http://nationality"));
        registry.register("league", () -> getHealth("http://league"));
        registry.register("nationalteam", () -> getHealth("http://nationalteam"));
        registry.register("player-composite", () -> getHealth("http://player-composite"));
        return new CompositeReactiveHealthIndicator(healthAggregator, registry);
    }

    private Mono<Health> getHealth(String url) {
        url += "/actuator/health";
        LOG.debug("Will call the Health API on URL: {}", url);

        return getWebClient().get().uri(url).retrieve().bodyToMono(String.class)
                .map(s -> new Health.Builder().up().build())
                .onErrorResume(ex -> Mono.just(new Health.Builder().down(ex).build()))
                .log();
    }

    private WebClient getWebClient() {
        if (webClient == null) {
            webClient = webClientBuilder.build();
        }
        return webClient;
    }
}