package se.magnus.microservices.core.team;

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
import se.magnus.api.core.team.Team;
import se.magnus.api.event.Event;
import se.magnus.microservices.core.team.persistence.TeamRepository;
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
        "spring.datasource.url=jdbc:h2:mem:team-db",
        "server.error.include-message=always"})
public class TeamServiceApplicationTests {
    @Autowired
    private WebTestClient client;

    @Autowired
    private TeamRepository repository;

    @Autowired
    private Sink channels;

    private AbstractMessageChannel input = null;

    @Before
    public void setupDb() {
        input = (AbstractMessageChannel) channels.input();
        repository.deleteAll().block();
    }

    @Test
    public void getTeamById() {
        int teamId = 1;
        assertNull(repository.findByTeamId(teamId));
        assertEquals(0, repository.count());
        sendCreateTeamEvent(teamId);
        assertNotNull(repository.findByTeamId(teamId));
        assertEquals(1, repository.count());

        getAndVerifyTeam(teamId, OK)
                .jsonPath("$.playerId").isEqualTo(teamId);
    }

    @Test
    public void duplicateError() {
        int teamId = 1;
        assertEquals(0, repository.count());
        sendCreateTeamEvent(teamId);
        assertEquals(1, repository.count());

        try {
            sendCreateTeamEvent(teamId);
            fail("Expected a MessagingException here!");
        } catch (MessagingException me) {
            if (me.getCause() instanceof InvalidInputException) {
                InvalidInputException iie = (InvalidInputException) me.getCause();
                assertEquals("Duplicate key, Team Id: 1, Review Id:1", iie.getMessage());
            } else {
                fail("Expected a InvalidInputException as the root cause!");
            }
        }

        assertEquals(1, repository.count());
    }

    @Test
    public void deleteTeam() {
        int teamId = 1;
        sendCreateTeamEvent(teamId);
        assertNotNull(repository.findByTeamId(teamId));
        sendCreateTeamEvent(teamId);
        assertNotNull(repository.findByTeamId(teamId));
        sendCreateTeamEvent(teamId);
    }

    @Test
    public void getTeamInvalidParameterString() {
        getAndVerifyTeam("/no-integer", BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/team/no-integer")
                .jsonPath("$.message").isEqualTo("Type mismatch.");
    }

    @Test
    public void getTeamNotFound() {
        int teamIdNotFound = 13;

        getAndVerifyTeam(teamIdNotFound, NOT_FOUND)
                .jsonPath("$.path").isEqualTo("/team/" + teamIdNotFound)
                .jsonPath("$.message").isEqualTo("No team found for teamId: " + teamIdNotFound);
    }

    @Test
    public void getTeamInvalidParameterNegativeValue() {
        int teamIdInvalid = -1;

        getAndVerifyTeam(teamIdInvalid, UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/team/" + teamIdInvalid)
                .jsonPath("$.message").isEqualTo("Invalid teamId: " + teamIdInvalid);
    }

    private WebTestClient.BodyContentSpec getAndVerifyTeam(int teamId, HttpStatus expectedStatus) {
        return getAndVerifyTeam("/" + teamId, expectedStatus);
    }

    private WebTestClient.BodyContentSpec getAndVerifyTeam(String teamIdPath, HttpStatus expectedStatus) {
        return client.get()
                .uri("/team" + teamIdPath)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();
    }

    private void sendCreateTeamEvent(int teamId) {
        Team team = new Team(teamId, 1, "Name " + teamId, "Founded " + teamId, "City" + teamId, "SA");
        Event<Integer, Team> event = new Event(CREATE, teamId, team);
        input.send(new GenericMessage<>(event));
    }

    private void sendDeleteTeamEvent(int teamId) {
        Event<Integer, Team> event = new Event(DELETE, teamId, null);
        input.send(new GenericMessage<>(event));
    }
}
