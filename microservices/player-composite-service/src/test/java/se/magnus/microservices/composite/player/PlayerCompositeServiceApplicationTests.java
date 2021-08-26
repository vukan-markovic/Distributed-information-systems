package se.magnus.microservices.composite.player;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.magnus.api.composite.player.*;
import se.magnus.api.core.league.League;
import se.magnus.api.core.nationality.Nationality;
import se.magnus.api.core.nationalteam.NationalTeam;
import se.magnus.api.core.player.Player;
import se.magnus.api.core.team.Team;
import se.magnus.microservices.composite.player.services.PlayerCompositeIntegration;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.exceptions.NotFoundException;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;

@SuppressWarnings("ALL")
@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = RANDOM_PORT,
        classes = {PlayerCompositeServiceApplication.class, TestSecurityConfig.class},
        properties = {"spring.main.allow-bean-definition-overriding=true", "eureka.client.enabled=false", "spring.cloud.config.enabled=false"})
public class PlayerCompositeServiceApplicationTests {
    private static final int ID_OK = 1;
    private static final int PLAYER_ID_NOT_FOUND = 2;
    private static final int PLAYER_ID_INVALID = 3;

    @Autowired
    private WebTestClient client;

    @MockBean
    private PlayerCompositeIntegration compositeIntegration;

    @Before
    public void setUp() {
        when(compositeIntegration.getPlayer(eq(ID_OK))).
                thenReturn(new Player(ID_OK, "name", "surname", "reg number", "birth", 1, 1, 1, 1, "mock-address"));

        when(compositeIntegration.getTeam(ID_OK)).
                thenReturn((new Team(ID_OK, "c", "a", "b", "mock address")));

        when(compositeIntegration.getNationalTeam(ID_OK)).
                thenReturn(new NationalTeam(ID_OK, "a", "b", "mock address"));

        when(compositeIntegration.getNationality(ID_OK)).
                thenReturn(new Nationality(ID_OK, "a", "c", "mock address"));

        when(compositeIntegration.getLeague(ID_OK)).
                thenReturn(new League(ID_OK, "c", "a", "mock address"));

        when(compositeIntegration.getPlayer(eq(PLAYER_ID_NOT_FOUND))).thenThrow(new NotFoundException("NOT FOUND: " + PLAYER_ID_NOT_FOUND));
        when(compositeIntegration.getPlayer(eq(PLAYER_ID_INVALID))).thenThrow(new InvalidInputException("INVALID: " + PLAYER_ID_INVALID));
    }

    @Test
    public void contextLoads() {
    }

    @Test
    public void createCompositeProduct1() {
        PlayerAggregate compositePlayer = new PlayerAggregate(1, "name", "surname", "registration number", "date of birth",
                null, null, null, null, null);

        postAndVerifyPlayer(compositePlayer, INTERNAL_SERVER_ERROR);
    }

    @Test
    public void createCompositeProduct2() {
        PlayerAggregate compositePlayer = new PlayerAggregate(1, "name", "surname", "registration number", "date of birth",
                new TeamSummary(1, "a", "b", "c"),
                new NationalitySummary(1, "a", "s"),
                new NationalTeamSummary(1, "a", "s"),
                new LeagueSummary(1, "a", "s"), null);

        postAndVerifyPlayer(compositePlayer, OK);
    }

    @Test
    public void deleteCompositeProduct() {
        PlayerAggregate compositePlayer = new PlayerAggregate(1, "name", "surname", "registration number", "date of birth",
                new TeamSummary(1, "a", "b", "c"),
                new NationalitySummary(1, "a", "s"),
                new NationalTeamSummary(1, "a", "s"),
                new LeagueSummary(1, "a", "s"), null);

        postAndVerifyPlayer(compositePlayer, OK);
        deleteAndVerifyPlayer(compositePlayer.getPlayerId(), OK);
    }

    @Test
    public void getPlayerById() {
        getAndVerifyPlayer(ID_OK, OK)
                .jsonPath("$.playerId").isEqualTo(ID_OK)
                .jsonPath("$.team").isNotEmpty()
                .jsonPath("$.nationality").isNotEmpty()
                .jsonPath("$.league").isNotEmpty();
    }

    @Test
    public void getPlayerNotFound() {
        getAndVerifyPlayer(PLAYER_ID_NOT_FOUND, NOT_FOUND)
                .jsonPath("$.path").isEqualTo("/player-composite/" + PLAYER_ID_NOT_FOUND)
                .jsonPath("$.message").isEqualTo("NOT FOUND: " + PLAYER_ID_NOT_FOUND);
    }

    @Test
    public void getPlayerInvalidInput() {
        getAndVerifyPlayer(PLAYER_ID_INVALID, UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/player-composite/" + PLAYER_ID_INVALID)
                .jsonPath("$.message").isEqualTo("INVALID: " + PLAYER_ID_INVALID);
    }

    private WebTestClient.BodyContentSpec getAndVerifyPlayer(int playerId, HttpStatus expectedStatus) {
        return client.get()
                .uri("/player-composite/" + playerId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();
    }

    private void postAndVerifyPlayer(PlayerAggregate compositePlayer, HttpStatus expectedStatus) {
        client.post()
                .uri("/player-composite")
                .body(just(compositePlayer), PlayerAggregate.class)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);
    }

    private void deleteAndVerifyPlayer(int playerId, HttpStatus expectedStatus) {
        client.delete()
                .uri("/player-composite/" + playerId)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);
    }
}