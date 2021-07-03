package se.magnus.microservices.core.nationalTeam;

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
import se.magnus.api.core.nationalTeam.NationalTeam;
import se.magnus.api.core.player.Player;
import se.magnus.api.event.Event;
import se.magnus.microservices.core.nationalTeam.persistence.NationalTeamRepository;
import se.magnus.util.exceptions.InvalidInputException;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static se.magnus.api.event.Event.Type.CREATE;
import static se.magnus.api.event.Event.Type.DELETE;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=RANDOM_PORT, properties = {"spring.data.mongodb.port: 0", "eureka.client.enabled=false", "spring.cloud.config.enabled=false", "server.error.include-message=always"})
public class NationalTeamServiceApplicationTests {
    @Autowired
    private WebTestClient client;

    @Autowired
    private NationalTeamRepository repository;

    @Autowired
    private Sink channels;

    private AbstractMessageChannel input = null;

    @Before
    public void setupDb() {
        input = (AbstractMessageChannel) channels.input();
        repository.deleteAll();
    }

    @Test
    public void getNationalTeamById() {
        int nationalTeamId = 1;
        assertNull(repository.findByNationalTeamId(nationalTeamId));
        assertEquals(0, repository.count());
        sendCreateNationalTeamEvent(nationalTeamId);
        assertNotNull(repository.findByNationalTeamId(nationalTeamId));
        assertEquals(1, repository.count());

        getAndVerifyNationalTeam(nationalTeamId, OK)
                .jsonPath("$.nationalTeamId").isEqualTo(nationalTeamId);
    }

    @Test
    public void duplicateError() {
        int nationalTeamId = 1;
        assertEquals(0, repository.count());
        sendCreateNationalTeamEvent(nationalTeamId);
        assertEquals(1, repository.count());

        try {
            sendCreateNationalTeamEvent(nationalTeamId);
            fail("Expected a MessagingException here!");
        } catch (MessagingException me) {
            if (me.getCause() instanceof InvalidInputException) {
                InvalidInputException iie = (InvalidInputException) me.getCause();
                assertEquals("Duplicate key, Player Id: 1, Review Id:1", iie.getMessage());
            } else {
                fail("Expected a InvalidInputException as the root cause!");
            }
        }

        assertEquals(1, repository.count());
    }

    @Test
    public void deleteNationalTeam() {
        int nationalTeamId = 1;
        sendCreateNationalTeamEvent(nationalTeamId);
        assertNotNull(repository.findByNationalTeamId(nationalTeamId));
        sendDeleteNationalTeamEvent(nationalTeamId);
        assertNotNull(repository.findByNationalTeamId(nationalTeamId));
        sendDeleteNationalTeamEvent(nationalTeamId);
    }

    @Test
    public void getNationalTeamInvalidParameterString() {
        getAndVerifyNationalTeam("/no-integer", BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/nationalTeam/no-integer")
                .jsonPath("$.message").isEqualTo("Type mismatch.");
    }

    @Test
    public void getNationalTeamNotFound() {
        int nationalTeamIdNotFound = 13;

        getAndVerifyNationalTeam(nationalTeamIdNotFound, NOT_FOUND)
                .jsonPath("$.path").isEqualTo("/nationalTeam/" + nationalTeamIdNotFound)
                .jsonPath("$.message").isEqualTo("No nationalTeam found for nationalTeamId: " + nationalTeamIdNotFound);
    }

    @Test
    public void getNationalTeamInvalidParameterNegativeValue() {
        int nationalTeamIdInvalid = -1;

        getAndVerifyNationalTeam(nationalTeamIdInvalid, UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/nationalTeam/" + nationalTeamIdInvalid)
                .jsonPath("$.message").isEqualTo("Invalid nationalTeamId: " + nationalTeamIdInvalid);
    }

    private WebTestClient.BodyContentSpec getAndVerifyNationalTeam(int nationalTeamId, HttpStatus expectedStatus) {
        return getAndVerifyNationalTeam("/" + nationalTeamId, expectedStatus);
    }

    private WebTestClient.BodyContentSpec getAndVerifyNationalTeam(String nationalTeamIdPath, HttpStatus expectedStatus) {
        return client.get()
                .uri("/nationalTeam" + nationalTeamIdPath)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();
    }

    private void sendCreateNationalTeamEvent(int nationalTeamId) {
        NationalTeam nationalTeam = new NationalTeam(nationalTeamId, "Name " + nationalTeamId, "Abbreviation " + nationalTeamId, "SA");
        Event<Integer, NationalTeam> event = new Event(CREATE, nationalTeamId, nationalTeam);
        input.send(new GenericMessage<>(event));
    }

    private void sendDeleteNationalTeamEvent(int nationalTeamId) {
        Event<Integer, Player> event = new Event(DELETE, nationalTeamId, null);
        input.send(new GenericMessage<>(event));
    }
}