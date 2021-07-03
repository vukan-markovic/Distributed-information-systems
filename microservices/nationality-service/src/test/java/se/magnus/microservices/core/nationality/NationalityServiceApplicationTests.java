package se.magnus.microservices.core.nationality;

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
import se.magnus.api.core.nationality.Nationality;
import se.magnus.api.core.player.Player;
import se.magnus.api.event.Event;
import se.magnus.microservices.core.nationality.persistence.NationalityRepository;
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
        "spring.datasource.url=jdbc:h2:mem:nationality-db",
        "server.error.include-message=always"})
public class NationalityServiceApplicationTests {
    @Autowired
    private WebTestClient client;

    @Autowired
    private NationalityRepository repository;

    @Autowired
    private Sink channels;

    private AbstractMessageChannel input = null;

    @Before
    public void setupDb() {
        input = (AbstractMessageChannel) channels.input();
        repository.deleteAll();
    }

    @Test
    public void getNationalityById() {
        int nationalityId = 1;
        assertNull(repository.findByNationalityId(nationalityId));
        assertEquals(0, repository.count());
        sendCreateNationalityEvent(nationalityId);
        assertNotNull(repository.findByNationalityId(nationalityId));
        assertEquals(1, repository.count());

        getAndVerifyNationality(nationalityId, OK)
                .jsonPath("$.nationalityId").isEqualTo(nationalityId);
    }

    @Test
    public void duplicateError() {
        int nationalityId = 1;
        assertEquals(0, repository.count());
        sendCreateNationalityEvent(nationalityId);
        assertEquals(1, repository.count());

        try {
            sendCreateNationalityEvent(nationalityId);
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
    public void deleteNationality() {
        int nationalityId = 1;
        sendCreateNationalityEvent(nationalityId);
        assertNotNull(repository.findByNationalityId(nationalityId));
        sendDeleteNationalityEvent(nationalityId);
        assertNotNull(repository.findByNationalityId(nationalityId));
        sendDeleteNationalityEvent(nationalityId);
    }

    @Test
    public void getNationalityInvalidParameterString() {
        getAndVerifyNationality("/no-integer", BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/nationality/no-integer")
                .jsonPath("$.message").isEqualTo("Type mismatch.");
    }

    @Test
    public void getNationalityNotFound() {
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

    private void sendCreateNationalityEvent(int nationalityId) {
        Nationality nationalTeam = new Nationality(nationalityId, "Name " + nationalityId, "Abbreviation " + nationalityId, "SA");
        Event<Integer, Nationality> event = new Event(CREATE, nationalityId, nationalTeam);
        input.send(new GenericMessage<>(event));
    }

    private void sendDeleteNationalityEvent(int nationalityId) {
        Event<Integer, Player> event = new Event(DELETE, nationalityId, null);
        input.send(new GenericMessage<>(event));
    }
}