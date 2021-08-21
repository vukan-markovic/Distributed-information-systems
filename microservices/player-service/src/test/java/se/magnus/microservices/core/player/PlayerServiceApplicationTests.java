package se.magnus.microservices.core.player;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.http.HttpStatus;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.magnus.api.core.player.Player;
import se.magnus.api.event.Event;
import se.magnus.microservices.core.player.persistence.PlayerRepository;
import se.magnus.util.exceptions.InvalidInputException;

import static org.junit.Assert.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static se.magnus.api.event.Event.Type.CREATE;
import static se.magnus.api.event.Event.Type.DELETE;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"spring.data.mongodb.port: 0", "eureka.client.enabled=false", "spring.cloud.config.enabled=false", "server.error.include-message=always"})
public class PlayerServiceApplicationTests {

    @Autowired
    private WebTestClient client;

    @Autowired
    private PlayerRepository repository;

    @Autowired
    private Sink channels;

    private AbstractMessageChannel input = null;

    @Before
    public void setupDb() {
        input = (AbstractMessageChannel) channels.input();
        repository.deleteAll().block();
    }

    @Test
    public void getPlayerById() {
        int playerId = 1;
        assertNull(repository.findByPlayerId(playerId).block());
        assertEquals(0, (long) repository.count().block());
        sendCreatePlayerEvent(playerId);
        assertNotNull(repository.findByPlayerId(playerId).block());
        assertEquals(1, (long) repository.count().block());

        getAndVerifyPlayer(playerId, OK)
                .jsonPath("$.playerId").isEqualTo(playerId);
    }

    @Test
    public void duplicateError() {
        int playerId = 1;
        assertNull(repository.findByPlayerId(playerId).block());
        sendCreatePlayerEvent(playerId);
        assertNotNull(repository.findByPlayerId(playerId).block());

        try {
            sendCreatePlayerEvent(playerId);
            fail("Expected a MessagingException here!");
        } catch (MessagingException me) {
            if (me.getCause() instanceof InvalidInputException) {
                InvalidInputException iie = (InvalidInputException) me.getCause();
                assertEquals("Duplicate key, Player Id: " + playerId, iie.getMessage());
            } else {
                fail("Expected a InvalidInputException as the root cause!");
            }
        }
    }

    @Test
    public void deletePlayer() {
        int playerId = 1;
        sendCreatePlayerEvent(playerId);
        assertNotNull(repository.findByPlayerId(playerId).block());
        sendDeletePlayerEvent(playerId);
        assertNull(repository.findByPlayerId(playerId).block());
        sendDeletePlayerEvent(playerId);
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

    private void sendCreatePlayerEvent(int playerId) {
        Player player = new Player(playerId, "Name " + playerId, "Surname " + playerId, "Reg number " + playerId, "02.02.2021." + playerId, 1, 1, 1, 1, "SA");
        Event<Integer, Player> event = new Event(CREATE, playerId, player);
        input.send(new GenericMessage<>(event));
    }

    private void sendDeletePlayerEvent(int playerId) {
        Event<Integer, Player> event = new Event(DELETE, playerId, null);
        input.send(new GenericMessage<>(event));
    }
}