package se.magnus.microservices.core.league;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.magnus.api.core.league.League;
import se.magnus.microservices.core.league.persistence.LeagueRepository;

import static org.junit.Assert.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;

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

    @Before
    public void setupDb() {
        repository.deleteAll();
    }

    @Test
    public void getLeagueById() {
        int leagueId = 1;
        postAndVerifyLeague(leagueId, OK);
        assertTrue(repository.findByLeagueId(leagueId).isPresent());

        getAndVerifyLeague(leagueId, OK)
                .jsonPath("$.leagueId").isEqualTo(leagueId);
    }

    @Test
    public void duplicateError() {
        int leagueId = 1;
        postAndVerifyLeague(leagueId, OK);
        assertTrue(repository.findByLeagueId(leagueId).isPresent());

        postAndVerifyLeague(leagueId, UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/league")
                .jsonPath("$.message").isEqualTo("Duplicate key, League Id: " + leagueId);
    }

    @Test
    public void deleteLeague() {
        int leagueId = 1;
        postAndVerifyLeague(leagueId, OK);
        assertTrue(repository.findByLeagueId(leagueId).isPresent());
        deleteAndVerifyLeague(leagueId, OK);
        assertFalse(repository.findByLeagueId(leagueId).isPresent());
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

    private WebTestClient.BodyContentSpec postAndVerifyLeague(int leagueId, HttpStatus expectedStatus) {
        League league = new League(leagueId, "Name " + leagueId, "Label " + leagueId, "SA");

        return client.post()
                .uri("/league")
                .body(just(league), League.class)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();
    }

    private WebTestClient.BodyContentSpec deleteAndVerifyLeague(int leagueId, HttpStatus expectedStatus) {
        return client.delete()
                .uri("/league/" + leagueId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectBody();
    }
}