package se.magnus.microservices.core.nationalteam;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.magnus.api.core.nationalteam.NationalTeam;
import se.magnus.microservices.core.nationalteam.persistence.NationalTeamRepository;

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
        "spring.datasource.url=jdbc:h2:mem:nationalteam-db",
        "server.error.include-message=always"})
public class NationalTeamServiceApplicationTests {
    @Autowired
    private WebTestClient client;

    @Autowired
    private NationalTeamRepository repository;

    @Before
    public void setupDb() {
        repository.deleteAll();
    }

    @Test
    public void getNationalTeamById() {
        int nationalteamId = 1;
        postAndVerifyNationalTeam(nationalteamId, OK);
        assertTrue(repository.findByNationalteamId(nationalteamId).isPresent());
    }

    @Test
    public void duplicateError() {
        int nationalteamId = 1;
        postAndVerifyNationalTeam(nationalteamId, OK);
        assertTrue(repository.findByNationalteamId(nationalteamId).isPresent());

        postAndVerifyNationalTeam(nationalteamId, UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/nationalteam")
                .jsonPath("$.message").isEqualTo("Duplicate key, National team Id: " + nationalteamId);
    }

    @Test
    public void deleteNationalTeam() {
        int nationalteamId = 1;
        postAndVerifyNationalTeam(nationalteamId, OK);
        assertTrue(repository.findByNationalteamId(nationalteamId).isPresent());
        deleteAndVerifyNationalTeam(nationalteamId, OK);
        assertFalse(repository.findByNationalteamId(nationalteamId).isPresent());
    }

    @Test
    public void getNationalTeamInvalidParameterString() {
        getAndVerifyNationalTeam("/no-integer", BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/nationalteam/no-integer")
                .jsonPath("$.message").isEqualTo("Type mismatch.");
    }

    @Test
    public void getNationalTeamNotFound() {
        int nationalteamIdNotFound = 13;

        getAndVerifyNationalTeam(nationalteamIdNotFound, NOT_FOUND)
                .jsonPath("$.path").isEqualTo("/nationalteam/" + nationalteamIdNotFound)
                .jsonPath("$.message").isEqualTo("No national team found for nationalteamId: " + nationalteamIdNotFound);
    }

    @Test
    public void getNationalTeamInvalidParameterNegativeValue() {
        int nationalteamIdInvalid = -1;

        getAndVerifyNationalTeam(nationalteamIdInvalid, UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/nationalteam/" + nationalteamIdInvalid)
                .jsonPath("$.message").isEqualTo("Invalid nationalteamId: " + nationalteamIdInvalid);
    }

    private WebTestClient.BodyContentSpec getAndVerifyNationalTeam(int nationalteamId, HttpStatus expectedStatus) {
        return getAndVerifyNationalTeam("/" + nationalteamId, expectedStatus);
    }

    private WebTestClient.BodyContentSpec getAndVerifyNationalTeam(String nationalteamIdPath, HttpStatus expectedStatus) {
        return client.get()
                .uri("/nationalteam" + nationalteamIdPath)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();
    }

    private WebTestClient.BodyContentSpec postAndVerifyNationalTeam(int nationalTeamId, HttpStatus expectedStatus) {
        NationalTeam nationalTeam = new NationalTeam(nationalTeamId, "Name " + nationalTeamId, "Team selector " + nationalTeamId, "SA");

        return client.post()
                .uri("/nationalteam")
                .body(just(nationalTeam), NationalTeam.class)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();
    }

    private WebTestClient.BodyContentSpec deleteAndVerifyNationalTeam(int leagueId, HttpStatus expectedStatus) {
        return client.delete()
                .uri("/nationalteam/" + leagueId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectBody();
    }
}