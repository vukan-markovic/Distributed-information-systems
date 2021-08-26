package se.magnus.microservices.core.team;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.magnus.api.core.team.Team;
import se.magnus.microservices.core.team.persistence.TeamRepository;

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
        "spring.datasource.url=jdbc:h2:mem:team-db",
        "server.error.include-message=always"})
public class TeamServiceApplicationTests {
    @Autowired
    private WebTestClient client;

    @Autowired
    private TeamRepository repository;

    @Before
    public void setupDb() {
        repository.deleteAll();
    }

    @Test
    public void getTeamById() {
        int teamId = 1;
        postAndVerifyTeam(teamId, OK);
        assertTrue(repository.findByTeamId(teamId).isPresent());

        getAndVerifyTeam(teamId, OK)
                .jsonPath("$.teamId").isEqualTo(teamId);
    }

    @Test
    public void duplicateError() {
        int teamId = 1;
        postAndVerifyTeam(teamId, OK);
        assertTrue(repository.findByTeamId(teamId).isPresent());

        postAndVerifyTeam(teamId, UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/team")
                .jsonPath("$.message").isEqualTo("Duplicate key, Team Id: 1");
    }

    @Test
    public void deleteTeam() {
        int teamId = 1;
        postAndVerifyTeam(teamId, OK);
        assertTrue(repository.findByTeamId(teamId).isPresent());
        deleteAndVerifyTeam(teamId, OK);
        assertFalse(repository.findByTeamId(teamId).isPresent());
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

    private WebTestClient.BodyContentSpec postAndVerifyTeam(int teamId, HttpStatus expectedStatus) {
        Team team = new Team(teamId, "Name " + teamId, "Founded " + teamId, "City " + teamId, "SA");

        return client.post()
                .uri("/team")
                .body(just(team), Team.class)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();
    }

    private WebTestClient.BodyContentSpec deleteAndVerifyTeam(int teamId, HttpStatus expectedStatus) {
        return client.delete()
                .uri("/team/" + teamId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectBody();
    }
}