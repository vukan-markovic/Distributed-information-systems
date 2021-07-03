package se.magnus.microservices.core.league;

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
import se.magnus.api.core.league.League;
import se.magnus.api.event.Event;
import se.magnus.microservices.core.league.persistence.LeagueRepository;
import se.magnus.util.exceptions.InvalidInputException;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static se.magnus.api.event.Event.Type.CREATE;
import static se.magnus.api.event.Event.Type.DELETE;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {
        "logging.level.se.magnus=DEBUG",
        "eureka.client.enabled=false",
        "spring.cloud.config.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:league-db",
        "server.error.include-message=always"})
public class LeagueServiceApplicationTests {
    @Autowired
    private WebTestClient client;

    @Autowired
    private LeagueRepository repository;

    @Autowired
    private Sink channels;

    private AbstractMessageChannel input = null;

    @Before
    public void setupDb() {
        input = (AbstractMessageChannel) channels.input();
        repository.deleteAll();
    }

    @Test
    public void getLeagueById() {
        int leagueId = 1;
        assertNull(repository.findByLeagueId(leagueId));
        assertEquals(0, repository.count());
        sendCreateLeagueEvent(leagueId);
        assertNotNull(repository.findByLeagueId(leagueId));
        assertEquals(1, repository.count());

        getAndVerifyLeague(leagueId, OK)
                .jsonPath("$.playerId").isEqualTo(leagueId);
    }

    @Test
    public void duplicateError() {
        int leagueId = 1;
        assertEquals(0, repository.count());
        sendCreateLeagueEvent(leagueId);
        assertEquals(1, repository.count());

        try {
            sendCreateLeagueEvent(leagueId);
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
    public void deleteLeague() {
        int leagueId = 1;
        sendCreateLeagueEvent(leagueId);
        assertNotNull(repository.findByLeagueId(leagueId));
        sendCreateLeagueEvent(leagueId);
        assertNotNull(repository.findByLeagueId(leagueId));
        sendCreateLeagueEvent(leagueId);
    }

    @Test
    public void getLeagueInvalidParameterString() {
        getAndVerifyLeague("/no-integer", BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/league/no-integer")
                .jsonPath("$.message").isEqualTo("Type mismatch.");
    }

    @Test
    public void getLeagueNotFound() {
        int leagueIdNotFound = 13;

        getAndVerifyLeague(leagueIdNotFound, NOT_FOUND)
                .jsonPath("$.path").isEqualTo("/league/" + leagueIdNotFound)
                .jsonPath("$.message").isEqualTo("No league found for leagueId: " + leagueIdNotFound);
    }

    @Test
    public void getLeagueInvalidParameterNegativeValue() {
        int leagueIdInvalid = -1;

        getAndVerifyLeague(leagueIdInvalid, UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/league/" + leagueIdInvalid)
                .jsonPath("$.message").isEqualTo("Invalid leagueId: " + leagueIdInvalid);
    }

    private WebTestClient.BodyContentSpec getAndVerifyLeague(int leagueId, HttpStatus expectedStatus) {
        return getAndVerifyLeague("/" + leagueId, expectedStatus);
    }

    private WebTestClient.BodyContentSpec getAndVerifyLeague(String leagueIdPath, HttpStatus expectedStatus) {
        return client.get()
                .uri("/league" + leagueIdPath)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();
    }

    private void sendCreateLeagueEvent(int leagueId) {
        League league = new League(leagueId, "Name " + leagueId, "Label " + leagueId, "SA");
        Event<Integer, League> event = new Event(CREATE, leagueId, league);
        input.send(new GenericMessage<>(event));
    }

    private void sendDeleteLeagueEvent(int leagueId) {
        Event<Integer, League> event = new Event(DELETE, leagueId, null);
        input.send(new GenericMessage<>(event));
    }
}