package se.magnus.microservices.composite.player.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import se.magnus.api.core.nationalTeam.NationalTeam;
import se.magnus.api.core.nationalTeam.NationalTeamService;
import se.magnus.api.core.player.Player;
import se.magnus.api.core.player.PlayerService;
import se.magnus.api.core.team.Team;
import se.magnus.api.core.team.TeamService;
import se.magnus.api.event.Event;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.HttpErrorInfo;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;

import static se.magnus.api.event.Event.Type.CREATE;
import static se.magnus.api.event.Event.Type.DELETE;

@EnableBinding(PlayerCompositeIntegration.MessageSources.class)
@Component
public class PlayerCompositeIntegration implements PlayerService, TeamService, NationalTeamService {
    private static final Logger LOG = LoggerFactory.getLogger(PlayerCompositeIntegration.class);
    private final String playerServiceUrl = "http://player";
    private final String nationalTeamServiceUrl = "http://nationalTeam";
    private final String teamServiceUrl = "http://team";
    private final String nationalityServiceUrl = "http://nationality";
    private final ObjectMapper mapper;
    private final WebClient.Builder webClientBuilder;
    private final MessageSources messageSources;
    private final int playerServiceTimeoutSec;
    private WebClient webClient;

    @Autowired
    public PlayerCompositeIntegration(
            WebClient.Builder webClientBuilder,
            ObjectMapper mapper,
            MessageSources messageSources,
            @Value("${app.player-service.timeoutSec}") int playerServiceTimeoutSec

    ) {
        this.webClientBuilder = webClientBuilder;
        this.mapper = mapper;
        this.messageSources = messageSources;
        this.playerServiceTimeoutSec = playerServiceTimeoutSec;
    }

    @Override
    public Player createPlayer(Player body) {
        messageSources.outputPlayers().send(MessageBuilder.withPayload(new Event(CREATE, body.getPlayerId(), body)).build());
        return body;
    }

    @Retry(name = "player")
    @CircuitBreaker(name = "player")
    @Override
    public Mono<Player> getPlayer(int playerId, int delay, int faultPercent) {
        URI url = UriComponentsBuilder.fromUriString(playerServiceUrl + "/player/{playerId}?delay={delay}&faultPercent={faultPercent}").build(playerId, delay, faultPercent);
        LOG.debug("Will call the getPlayer API on URL: {}", url);

        return getWebClient().get().uri(url)
                .retrieve().bodyToMono(Player.class).log()
                .onErrorMap(WebClientResponseException.class, this::handleException)
                .timeout(Duration.ofSeconds(playerServiceTimeoutSec));
    }

    @Override
    public void deletePlayer(int playerId) {
        messageSources.outputPlayers().send(MessageBuilder.withPayload(new Event(DELETE, playerId, null)).build());
    }

    @Override
    public Team createTeam(Team body) {
        messageSources.outputTeams().send(MessageBuilder.withPayload(new Event(CREATE, body.getTeamId(), body)).build());
        return body;
    }

    @Override
    public Mono<Team> getTeam(int playerId) {
        URI url = UriComponentsBuilder.fromUriString(teamServiceUrl + "/recommendation?playerId={playerId}").build(playerId);
        LOG.debug("Will call the getRecommendations API on URL: {}", url);

        return getWebClient().get().uri(url)
                .retrieve().bodyToMono(Team.class).log()
                .onErrorMap(WebClientResponseException.class, this::handleException);
    }

    @Override
    public void deleteTeam(int playerId) {
        messageSources.outputTeams().send(MessageBuilder.withPayload(new Event(DELETE, playerId, null)).build());
    }

    @Override
    public NationalTeam createNationalTeam(NationalTeam body) {
        messageSources.outputNationalities().send(MessageBuilder.withPayload(new Event(CREATE, body.getNationalTeamId(), body)).build());
        return body;
    }

    @Override
    public Mono<NationalTeam> getNationalTeam(int playerId) {
        URI url = UriComponentsBuilder.fromUriString(nationalTeamServiceUrl + "/review?playerId={playerId}").build(playerId);
        LOG.debug("Will call the getReviews API on URL: {}", url);

        return getWebClient().get().uri(url)
                .retrieve().bodyToMono(NationalTeam.class).log()
                .onErrorMap(WebClientResponseException.class, this::handleException);
    }

    @Override
    public void deleteNationalTeam(int playerId) {
        messageSources.outputNationalities().send(MessageBuilder.withPayload(new Event(DELETE, playerId, null)).build());
    }

    private WebClient getWebClient() {
        if (webClient == null) webClient = webClientBuilder.build();
        return webClient;
    }

    private Throwable handleException(Throwable ex) {
        if (!(ex instanceof WebClientResponseException)) {
            LOG.warn("Got a unexpected error: {}, will rethrow it", ex.toString());
            return ex;
        }

        WebClientResponseException wcre = (WebClientResponseException) ex;

        switch (wcre.getStatusCode()) {
            case NOT_FOUND:
                return new NotFoundException(getErrorMessage(wcre));

            case UNPROCESSABLE_ENTITY:
                return new InvalidInputException(getErrorMessage(wcre));

            default:
                LOG.warn("Got a unexpected HTTP error: {}, will rethrow it", wcre.getStatusCode());
                LOG.warn("Error body: {}", wcre.getResponseBodyAsString());
                return ex;
        }
    }

    private String getErrorMessage(WebClientResponseException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ioex) {
            return ex.getMessage();
        }
    }

    public interface MessageSources {
        String OUTPUT_PRODUCTS = "output-players";
        String OUTPUT_TEAMS = "output-teams";
        String OUTPUT_NATIONALITIES = "output-nationalities";

        @Output(OUTPUT_PRODUCTS)
        MessageChannel outputPlayers();

        @Output(OUTPUT_TEAMS)
        MessageChannel outputTeams();

        @Output(OUTPUT_NATIONALITIES)
        MessageChannel outputNationalities();
    }
}