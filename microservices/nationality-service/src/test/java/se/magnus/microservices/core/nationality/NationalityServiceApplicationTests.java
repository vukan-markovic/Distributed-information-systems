package se.magnus.microservices.core.nationality;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.magnus.api.core.nationality.Nationality;
import se.magnus.microservices.core.nationality.persistence.NationalityRepository;

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
        "spring.datasource.url=jdbc:h2:mem:nationality-db",
        "server.error.include-message=always"})
public class NationalityServiceApplicationTests {
    @Autowired
    private WebTestClient client;

    @Autowired
    private NationalityRepository repository;

    @Before
    public void setupDb() {
        repository.deleteAll();
    }

    @Test
    public void getNationalityById() {
        int nationalityId = 1;
        postAndVerifyNationality(nationalityId, OK);
        assertTrue(repository.findByNationalityId(nationalityId).isPresent());

        getAndVerifyNationality(nationalityId, OK)
                .jsonPath("$.nationalityId").isEqualTo(nationalityId);
    }

    @Test
    public void duplicateError() {
        int nationalityId = 1;
        postAndVerifyNationality(nationalityId, OK);
        assertTrue(repository.findByNationalityId(nationalityId).isPresent());

        postAndVerifyNationality(nationalityId, UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/nationality")
                .jsonPath("$.message").isEqualTo("Duplicate key, Nationality Id: " + nationalityId);
    }

    @Test
    public void deleteNationality() {
        int nationalityId = 1;
        postAndVerifyNationality(nationalityId, OK);
        assertTrue(repository.findByNationalityId(nationalityId).isPresent());
        deleteAndVerifyNationality(nationalityId, OK);
        assertFalse(repository.findByNationalityId(nationalityId).isPresent());
    }

    @Test
    public void getNationalityInvalidParameterString() {
        getAndVerifyNationality("/no-integer", BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/nationality/no-integer")
                .jsonPath("$.message").isEqualTo("Type mismatch.");
    }

    @Test
    public void getLeagueNotFound() {
        int nationalityIdNotFound = 13;

        getAndVerifyNationality(nationalityIdNotFound, NOT_FOUND)
                .jsonPath("$.path").isEqualTo("/nationality/" + nationalityIdNotFound)
                .jsonPath("$.message").isEqualTo("No nationality found for nationalityId: " + nationalityIdNotFound);
    }

    @Test
    public void getNationalityInvalidParameterNegativeValue() {
        int nationalityIdInvalid = -1;

        getAndVerifyNationality(nationalityIdInvalid, UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/nationality/" + nationalityIdInvalid)
                .jsonPath("$.message").isEqualTo("Invalid nationalityId: " + nationalityIdInvalid);
    }

    private WebTestClient.BodyContentSpec getAndVerifyNationality(int nationalityId, HttpStatus expectedStatus) {
        return getAndVerifyNationality("/" + nationalityId, expectedStatus);
    }

    private WebTestClient.BodyContentSpec getAndVerifyNationality(String nationalityIdPath, HttpStatus expectedStatus) {
        return client.get()
                .uri("/nationality" + nationalityIdPath)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();
    }

    private WebTestClient.BodyContentSpec postAndVerifyNationality(int nationalityId, HttpStatus expectedStatus) {
        Nationality nationality = new Nationality(nationalityId, "Name " + nationalityId, "Abbreviation " + nationalityId, "SA");

        return client.post()
                .uri("/nationality")
                .body(just(nationality), Nationality.class)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();
    }

    private WebTestClient.BodyContentSpec deleteAndVerifyNationality(int nationalityId, HttpStatus expectedStatus) {
        return client.delete()
                .uri("/nationality/" + nationalityId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectBody();
    }
}