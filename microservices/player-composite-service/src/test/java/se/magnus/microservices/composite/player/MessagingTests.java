package se.magnus.microservices.composite.player;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.magnus.api.composite.player.*;
import se.magnus.api.core.nationalTeam.NationalTeam;
import se.magnus.api.core.player.Player;
import se.magnus.api.core.team.Team;
import se.magnus.api.event.Event;
import se.magnus.microservices.composite.player.services.PlayerCompositeIntegration;
import java.util.concurrent.BlockingQueue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.cloud.stream.test.matcher.MessageQueueMatcher.receivesPayloadThat;
import static org.springframework.http.HttpStatus.OK;
import static reactor.core.publisher.Mono.just;
import static se.magnus.api.event.Event.Type.CREATE;
import static se.magnus.api.event.Event.Type.DELETE;
import static se.magnus.microservices.composite.player.IsSameEvent.sameEventExceptCreatedAt;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = RANDOM_PORT,
        classes = {PlayerCompositeServiceApplication.class, TestSecurityConfig.class},
        properties = {"spring.main.allow-bean-definition-overriding=true", "eureka.client.enabled=false", "spring.cloud.config.enabled=false"})
public class MessagingTests {
    BlockingQueue<Message<?>> queuePlayers = null;
    BlockingQueue<Message<?>> queueRecommendations = null;
    BlockingQueue<Message<?>> queueReviews = null;

    @Autowired
    private WebTestClient client;

    @Autowired
    private PlayerCompositeIntegration.MessageSources channels;

    @Autowired
    private MessageCollector collector;

    @Before
    public void setUp() {
        queuePlayers = getQueue(channels.outputPlayers());
        queueRecommendations = getQueue(channels.outputTeams());
        queueReviews = getQueue(channels.outputNationalities());
    }

    @Test
    public void createCompositePlayer1() {
        PlayerAggregate composite = new PlayerAggregate(1, "name", "surname", "reg number", "a",
                new TeamSummary(1, "a", "c", "c", new LeagueSummary(1, "a", "b")),
                new NationalitySummary(1, "a", "s", "c"), new NationalTeamSummary(1, "a", "b", "c"), null);

        postAndVerifyPlayer(composite, OK);
        assertEquals(1, queuePlayers.size());
        Event<Integer, Player> expectedPlayerEvent = new Event(CREATE, composite.getPlayerId(), new Player(composite.getPlayerId(), composite.getName(), composite.getSurname(), composite.getRegistration_number(), composite.getDateOfBirth(), composite.getNationality().getNationalityId(), composite.getTeam().getTeamId(), composite.getNationalTeam().getNationalTeamId(), null));
        assertThat(queuePlayers, is(receivesPayloadThat(sameEventExceptCreatedAt(expectedPlayerEvent))));
        assertEquals(0, queueRecommendations.size());
        assertEquals(0, queueReviews.size());
    }

    @Test
    public void createCompositePlayer2() {
        PlayerAggregate composite = new PlayerAggregate(1, "name", "surname", "reg number", "a",
                new TeamSummary(1, "a", "c", "c", new LeagueSummary(1, "a", "b")),
                new NationalitySummary(1, "a", "s", "c"), new NationalTeamSummary(1, "a", "b", "c"), null);

        postAndVerifyPlayer(composite, OK);
        assertEquals(1, queuePlayers.size());
        Event<Integer, Player> expectedPlayerEvent = new Event(CREATE, composite.getPlayerId(), new Player(composite.getPlayerId(), composite.getName(), composite.getSurname(), composite.getRegistration_number(), composite.getDateOfBirth(), composite.getNationality().getNationalityId(), composite.getTeam().getTeamId(), composite.getNationalTeam().getNationalTeamId(), null));
        assertThat(queuePlayers, receivesPayloadThat(sameEventExceptCreatedAt(expectedPlayerEvent)));
        assertEquals(1, queueRecommendations.size());
        TeamSummary team = composite.getTeam();
        Event<Integer, Player> expectedRecommendationEvent = new Event(CREATE, composite.getPlayerId(), new Team(composite.getPlayerId(), team.getTeamId(), team.getName(), team.getFounded(), team.getCity(), null));
        assertThat(queueRecommendations, receivesPayloadThat(sameEventExceptCreatedAt(expectedRecommendationEvent)));
        assertEquals(1, queueReviews.size());
        NationalitySummary nationality = composite.getNationality();
        Event<Integer, Player> expectedReviewEvent = new Event(CREATE, composite.getPlayerId(), new NationalTeam(nationality.getNationalityId(), team.getName(), nationality.getAbbreviation(), null));
        assertThat(queueReviews, receivesPayloadThat(sameEventExceptCreatedAt(expectedReviewEvent)));
    }

    @Test
    public void deleteCompositePlayer() {
        deleteAndVerifyPlayer(1, OK);
        assertEquals(1, queuePlayers.size());
        Event<Integer, Player> expectedEvent = new Event(DELETE, 1, null);
        assertThat(queuePlayers, is(receivesPayloadThat(sameEventExceptCreatedAt(expectedEvent))));
        assertEquals(1, queueRecommendations.size());
        Event<Integer, Player> expectedRecommendationEvent = new Event(DELETE, 1, null);
        assertThat(queueRecommendations, receivesPayloadThat(sameEventExceptCreatedAt(expectedRecommendationEvent)));
        assertEquals(1, queueReviews.size());
        Event<Integer, Player> expectedReviewEvent = new Event(DELETE, 1, null);
        assertThat(queueReviews, receivesPayloadThat(sameEventExceptCreatedAt(expectedReviewEvent)));
    }

    private BlockingQueue<Message<?>> getQueue(MessageChannel messageChannel) {
        return collector.forChannel(messageChannel);
    }

    private void postAndVerifyPlayer(PlayerAggregate compositePlayer, HttpStatus expectedStatus) {
        client.post()
                .uri("/player-composite")
                .body(just(compositePlayer), PlayerAggregate.class)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);
    }

    private void deleteAndVerifyPlayer(int playerId, HttpStatus expectedStatus) {
        client.delete()
                .uri("/player-composite/" + playerId)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);
    }
}