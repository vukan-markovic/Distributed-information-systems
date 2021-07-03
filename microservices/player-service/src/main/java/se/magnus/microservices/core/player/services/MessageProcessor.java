package se.magnus.microservices.core.player.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import se.magnus.api.core.player.Player;
import se.magnus.api.core.player.PlayerService;
import se.magnus.api.event.Event;
import se.magnus.util.exceptions.EventProcessingException;

@EnableBinding(Sink.class)
public class MessageProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(MessageProcessor.class);
    private final PlayerService playerService;

    @Autowired
    public MessageProcessor(PlayerService playerService) {
        this.playerService = playerService;
    }

    @StreamListener(target = Sink.INPUT)
    public void process(Event<Integer, Player> event) {
        LOG.info("Process message created at {}...", event.getEventCreatedAt());

        switch (event.getEventType()) {
            case CREATE:
                Player player = event.getData();
                LOG.info("Create player with ID: {}", player.getPlayerId());
                playerService.createPlayer(player);
                break;

            case DELETE:
                int playerId = event.getKey();
                LOG.info("Delete recommendations with PlayerID: {}", playerId);
                playerService.deletePlayer(playerId);
                break;

            default:
                String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
                LOG.warn(errorMessage);
                throw new EventProcessingException(errorMessage);
        }

        LOG.info("Message processing done!");
    }
}