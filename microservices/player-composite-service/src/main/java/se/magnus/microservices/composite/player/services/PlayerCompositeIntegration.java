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
import se.magnus.api.core.league.League;
import se.magnus.api.core.league.LeagueService;
import se.magnus.api.core.nationality.Nationality;
import se.magnus.api.core.nationality.NationalityService;
import se.magnus.api.core.nationalteam.NationalTeam;
import se.magnus.api.core.nationalteam.NationalTeamService;
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
public class PlayerCompositeIntegration implements PlayerService, TeamService, NationalTeamService, NationalityService, LeagueService {
    private static final Logger LOG = LoggerFactory.getLogger(PlayerCompositeIntegration.class);
    private final String playerServiceUrl = "http://player";
    private final String nationalteamServiceUrl = "http://nationalteam";
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
    public Mono<Team> getTeam(int teamId) {
        URI url = UriComponentsBuilder.fromUriString(teamServiceUrl + "/team/{teamId}").build(teamId);
        LOG.debug("Will call the getTeam API on URL: {}", url);

        return getWebClient().get().uri(url)
                .retrieve().bodyToMono(Team.class).log()
                .onErrorMap(WebClientResponseException.class, this::handleException);
    }

    @Override
    public void deleteTeam(int teamId) {
        messageSources.outputTeams().send(MessageBuilder.withPayload(new Event(DELETE, teamId, null)).build());
    }

    @Override
    public NationalTeam createNationalTeam(NationalTeam body) {
        messageSources.outputNationalTeams().send(MessageBuilder.withPayload(new Event(CREATE, body.getNationalTeamId(), body)).build());
        return body;
    }

    @Override
    public Mono<NationalTeam> getNationalTeam(int nationalteamId) {
        URI url = UriComponentsBuilder.fromUriString(nationalteamServiceUrl + "/nationalteam/{nationalteamId}").build(nationalteamId);
        LOG.debug("Will call the getNationalTeam API on URL: {}", url);

        return getWebClient().get().uri(url)
                .retrieve().bodyToMono(NationalTeam.class).log()
                .onErrorMap(WebClientResponseException.class, this::handleException);
    }

    @Override
    public void deleteNationalTeam(int nationalteamId) {
        messageSources.outputNationalTeams().send(MessageBuilder.withPayload(new Event(DELETE, nationalteamId, null)).build());
    }

    @Override
    public Nationality createNationality(Nationality body) {
        messageSources.outputNationalities().send(MessageBuilder.withPayload(new Event(CREATE, body.getNationalityId(), body)).build());
        return body;
    }

    @Override
    public Mono<Nationality> getNationality(int nationalityId) {
        URI url = UriComponentsBuilder.fromUriString(nationalteamServiceUrl + "/nationality/{nationalityId}").build(nationalityId);
        LOG.debug("Will call the getNationality API on URL: {}", url);

        return getWebClient().get().uri(url)
                .retrieve().bodyToMono(Nationality.class).log()
                .onErrorMap(WebClientResponseException.class, this::handleException);
    }

    @Override
    public void deleteNationality(int nationalityId) {
        messageSources.outputNationalities().send(MessageBuilder.withPayload(new Event(DELETE, nationalityId, null)).build());
    }

    @Override
    public League createLeague(League body) {
        messageSources.outputNationalities().send(MessageBuilder.withPayload(new Event(CREATE, body.getLeagueId(), body)).build());
        return body;
    }

    @Override
    public Mono<League> getLeague(int leagueId) {
        URI url = UriComponentsBuilder.fromUriString(nationalteamServiceUrl + "/leagueId/{leagueId}").build(leagueId);
        LOG.debug("Will call the getLeague API on URL: {}", url);

        return getWebClient().get().uri(url)
                .retrieve().bodyToMono(League.class).log()
                .onErrorMap(WebClientResponseException.class, this::handleException);
    }

    @Override
    public void deleteLeague(int leagueId) {
        messageSources.outputLeagues().send(MessageBuilder.withPayload(new Event(DELETE, leagueId, null)).build());
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
        String OUTPUT_PLAYERS = "output-players";
        String OUTPUT_TEAMS = "output-teams";
        String OUTPUT_NATIONALITIES = "output-nationalities";
        String OUTPUT_NATIONALTEAMS = "output-nationalteams";
        String OUTPUT_LEAGUES = "output-leagues";

        @Output(OUTPUT_PLAYERS)
        MessageChannel outputPlayers();

        @Output(OUTPUT_TEAMS)
        MessageChannel outputTeams();

        @Output(OUTPUT_NATIONALITIES)
        MessageChannel outputNationalities();

        @Output(OUTPUT_NATIONALTEAMS)
        MessageChannel outputNationalTeams();

        @Output(OUTPUT_LEAGUES)
        MessageChannel outputLeagues();
    }
}