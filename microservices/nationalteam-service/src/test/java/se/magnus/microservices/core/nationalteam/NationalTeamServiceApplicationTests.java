//package se.magnus.microservices.core.nationalteam;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.cloud.stream.messaging.Sink;
//import org.springframework.http.HttpStatus;
//import org.springframework.integration.channel.AbstractMessageChannel;
//import org.springframework.messaging.MessagingException;
//import org.springframework.messaging.support.GenericMessage;
//import org.springframework.test.context.junit4.SpringRunner;
//import org.springframework.test.web.reactive.server.WebTestClient;
//import se.magnus.api.core.nationalteam.NationalTeam;
//import se.magnus.api.core.player.Player;
//import se.magnus.api.event.Event;
//import se.magnus.microservices.core.nationalteam.persistence.NationalTeamRepository;
//import se.magnus.util.exceptions.InvalidInputException;
//
//import static org.junit.Assert.*;
//import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
//import static org.springframework.http.HttpStatus.*;
//import static org.springframework.http.MediaType.APPLICATION_JSON;
//import static se.magnus.api.event.Event.Type.CREATE;
//import static se.magnus.api.event.Event.Type.DELETE;
//
//@RunWith(SpringRunner.class)
//@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"spring.data.mongodb.port: 0", "eureka.client.enabled=false", "spring.cloud.config.enabled=false", "server.error.include-message=always"})
//public class NationalTeamServiceApplicationTests {
//    @Autowired
//    private WebTestClient client;
//
//    @Autowired
//    private NationalTeamRepository repository;
//
//    @Autowired
//    private Sink channels;
//
//    private AbstractMessageChannel input = null;
//
//    @Before
//    public void setupDb() {
//        input = (AbstractMessageChannel) channels.input();
//        repository.deleteAll();
//    }
//
//    @Test
//    public void getNationalTeamById() {
//        int nationalteamId = 1;
//        assertNull(repository.findByNationalteamId(nationalteamId).block());
//        assertEquals(0, (long) repository.count().block());
//        sendCreateNationalTeamEvent(nationalteamId);
//        assertNotNull(repository.findByNationalteamId(nationalteamId).block());
//        assertEquals(1, (long) repository.count().block());
//
//        getAndVerifyNationalTeam(nationalteamId, OK)
//                .jsonPath("$.nationalteamId").isEqualTo(nationalteamId);
//    }
//
//    @Test
//    public void duplicateError() {
//        int nationalteamId = 1;
//        assertNull(repository.findByNationalteamId(nationalteamId).block());
//        sendCreateNationalTeamEvent(nationalteamId);
//        assertNotNull(repository.findByNationalteamId(nationalteamId).block());
//
//        try {
//            sendCreateNationalTeamEvent(nationalteamId);
//            fail("Expected a MessagingException here!");
//        } catch (MessagingException me) {
//            if (me.getCause() instanceof InvalidInputException) {
//                InvalidInputException iie = (InvalidInputException) me.getCause();
//                assertEquals("Duplicate key, National Team Id: 1", iie.getMessage());
//            } else fail("Expected a InvalidInputException as the root cause!");
//        }
//    }
//
//    @Test
//    public void deleteNationalTeam() {
//        int nationalteamId = 1;
//        sendCreateNationalTeamEvent(nationalteamId);
//        assertNotNull(repository.findByNationalteamId(nationalteamId).block());
//        sendDeleteNationalTeamEvent(nationalteamId);
//        assertNull(repository.findByNationalteamId(nationalteamId).block());
//    }
//
//    @Test
//    public void getNationalTeamInvalidParameterString() {
//        getAndVerifyNationalTeam("/no-integer", BAD_REQUEST)
//                .jsonPath("$.path").isEqualTo("/nationalteam/no-integer")
//                .jsonPath("$.message").isEqualTo("Type mismatch.");
//    }
//
//    @Test
//    public void getNationalTeamNotFound() {
//        int nationalteamIdNotFound = 13;
//
//        getAndVerifyNationalTeam(nationalteamIdNotFound, NOT_FOUND)
//                .jsonPath("$.path").isEqualTo("/nationalteam/" + nationalteamIdNotFound)
//                .jsonPath("$.message").isEqualTo("No national team found for nationalteamId: " + nationalteamIdNotFound);
//    }
//
//    @Test
//    public void getNationalTeamInvalidParameterNegativeValue() {
//        int nationalteamIdInvalid = -1;
//
//        getAndVerifyNationalTeam(nationalteamIdInvalid, UNPROCESSABLE_ENTITY)
//                .jsonPath("$.path").isEqualTo("/nationalteam/" + nationalteamIdInvalid)
//                .jsonPath("$.message").isEqualTo("Invalid nationalteamId: " + nationalteamIdInvalid);
//    }
//
//    private WebTestClient.BodyContentSpec getAndVerifyNationalTeam(int nationalteamId, HttpStatus expectedStatus) {
//        return getAndVerifyNationalTeam("/" + nationalteamId, expectedStatus);
//    }
//
//    private WebTestClient.BodyContentSpec getAndVerifyNationalTeam(String nationalteamIdPath, HttpStatus expectedStatus) {
//        return client.get()
//                .uri("/nationalteam" + nationalteamIdPath)
//                .accept(APPLICATION_JSON)
//                .exchange()
//                .expectStatus().isEqualTo(expectedStatus)
//                .expectHeader().contentType(APPLICATION_JSON)
//                .expectBody();
//    }
//
//    private void sendCreateNationalTeamEvent(int nationalteamId) {
//        NationalTeam nationalteam = new NationalTeam(nationalteamId, "Name " + nationalteamId, "Abbreviation " + nationalteamId, "SA");
//        Event<Integer, Player> event = new Event(CREATE, nationalteamId, nationalteam);
//        input.send(new GenericMessage<>(event));
//    }
//
//    private void sendDeleteNationalTeamEvent(int nationalteamId) {
//        Event<Integer, Player> event = new Event(DELETE, nationalteamId, null);
//        input.send(new GenericMessage<>(event));
//    }
//}