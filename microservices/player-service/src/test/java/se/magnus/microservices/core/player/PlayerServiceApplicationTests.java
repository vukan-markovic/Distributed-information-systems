package se.magnus.microservices.core.player;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.magnus.api.core.player.Player;
import se.magnus.microservices.core.player.persistence.PlayerRepository;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {
        "logging.level.se.magnus=DEBUG",
        "eureka.client.enabled=false",
        "spring.cloud.config.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:player-db",
        "server.error.include-message=always"})
public class PlayerServiceApplicationTests {

    @Autowired
    private WebTestClient client;

    @Autowired
    private PlayerRepository repository;

    @Before
    public void setupDb() {
        repository.deleteAll();
    }

    @Test
    public void getPlayerById() {
        int playerId = 1;
        postAndVerifyPlayer(playerId, OK);
        assertTrue(repository.findByPlayerId(playerId).isPresent());

        getAndVerifyPlayer(playerId, OK)
                .jsonPath("$.playerId").isEqualTo(playerId);
    }

    @Test
    public void duplicateError() {
        int playerId = 1;
        postAndVerifyPlayer(playerId, OK);
        assertTrue(repository.findByPlayerId(playerId).isPresent());

        postAndVerifyPlayer(playerId, UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/player")
                .jsonPath("$.message").isEqualTo("Duplicate key, Player Id: " + playerId);

    }

    @Test
    public void deletePlayer() {
        int playerId = 1;
        postAndVerifyPlayer(playerId, OK);
        assertTrue(repository.findByPlayerId(playerId).isPresent());
        deleteAndVerifyPlayer(playerId, OK);
        assertFalse(repository.findByPlayerId(playerId).isPresent());

    }

    @Test
    public void getPlayerInvalidParameterString() {
        getAndVerifyPlayer("/no-integer", BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/player/no-integer")
                .jsonPath("$.message").isEqualTo("Type mismatch.");
    }

    @Test
    public void getPlayerNotFound() {
        int playerIdNotFound = 13;

        getAndVerifyPlayer(playerIdNotFound, NOT_FOUND)
                .jsonPath("$.path").isEqualTo("/player/" + playerIdNotFound)
                .jsonPath("$.message").isEqualTo("No player found for playerId: " + playerIdNotFound);
    }

    @Test
    public void getPlayerInvalidParameterNegativeValue() {
        int playerIdInvalid = -1;

        getAndVerifyPlayer(playerIdInvalid, UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/player/" + playerIdInvalid)
                .jsonPath("$.message").isEqualTo("Invalid playerId: " + playerIdInvalid);
    }

    private WebTestClient.BodyContentSpec getAndVerifyPlayer(int playerId, HttpStatus expectedStatus) {
        return getAndVerifyPlayer("/" + playerId, expectedStatus);
    }

    private WebTestClient.BodyContentSpec getAndVerifyPlayer(String playerIdPath, HttpStatus expectedStatus) {
        return client.get()
                .uri("/player" + playerIdPath)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();
    }

    private WebTestClient.BodyContentSpec postAndVerifyPlayer(int playerId, HttpStatus expectedStatus) {
        Player player = new Player(playerId, "Name " + playerId, "Surname " + playerId, "Registration Number " + playerId, "Date of birth " + playerId, 1, 1, 1, 1, "SA");

        return client.post()
                .uri("/player")
                .body(just(player), Player.class)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();
    }

    private WebTestClient.BodyContentSpec deleteAndVerifyPlayer(int playerId, HttpStatus expectedStatus) {
        return client.delete()
                .uri("/player/" + playerId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectBody();
    }
}