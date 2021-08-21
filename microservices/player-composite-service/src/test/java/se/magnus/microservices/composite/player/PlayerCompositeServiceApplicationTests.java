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
import reactor.core.publisher.Mono;
import se.magnus.api.core.league.League;
import se.magnus.api.core.nationalteam.NationalTeam;
import se.magnus.api.core.player.Player;
import se.magnus.api.core.team.Team;
import se.magnus.microservices.composite.player.services.PlayerCompositeIntegration;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.exceptions.NotFoundException;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SuppressWarnings("ALL")
@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = RANDOM_PORT,
        classes = {PlayerCompositeServiceApplication.class, TestSecurityConfig.class},
        properties = {"spring.main.allow-bean-definition-overriding=true", "eureka.client.enabled=false", "spring.cloud.config.enabled=false"})
public class PlayerCompositeServiceApplicationTests {
    private static final int PLAYER_ID_OK = 1;
    private static final int PLAYER_ID_NOT_FOUND = 2;
    private static final int PLAYER_ID_INVALID = 3;

    @Autowired
    private WebTestClient client;

    @MockBean
    private PlayerCompositeIntegration compositeIntegration;

    @Before
    public void setUp() {
        when(compositeIntegration.getPlayer(eq(PLAYER_ID_OK), anyInt(), anyInt())).
                thenReturn(Mono.just(new Player(PLAYER_ID_OK, "name", "surname", "reg number", "birth", 1, 1, 1, 1, "mock-address")));

        when(compositeIntegration.getTeam(PLAYER_ID_OK)).
                thenReturn(Mono.just(new Team(1, "content", "a", "b", "mock address")));

        when(compositeIntegration.getNationalTeam(PLAYER_ID_OK)).
                thenReturn(Mono.just(new NationalTeam(1, "author", "subject", "mock address")));

        when(compositeIntegration.getLeague(PLAYER_ID_OK)).
                thenReturn(Mono.just(new League(1, "author", "subject", "mock address")));

        when(compositeIntegration.getPlayer(eq(PLAYER_ID_NOT_FOUND), anyInt(), anyInt())).thenThrow(new NotFoundException("NOT FOUND: " + PLAYER_ID_NOT_FOUND));
        when(compositeIntegration.getPlayer(eq(PLAYER_ID_INVALID), anyInt(), anyInt())).thenThrow(new InvalidInputException("INVALID: " + PLAYER_ID_INVALID));
    }

    @Test
    public void contextLoads() {
    }

    @Test
    public void getPlayerById() {
        getAndVerifyPlayer(PLAYER_ID_OK, OK)
                .jsonPath("$.playerId").isEqualTo(PLAYER_ID_OK)
                .jsonPath("$.recommendations.length()").isEqualTo(1)
                .jsonPath("$.reviews.length()").isEqualTo(1);
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
}