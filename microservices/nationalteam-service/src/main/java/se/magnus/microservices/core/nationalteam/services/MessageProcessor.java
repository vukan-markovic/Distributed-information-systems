package se.magnus.microservices.core.nationalteam.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import se.magnus.api.core.nationalteam.NationalTeam;
import se.magnus.api.core.nationalteam.NationalTeamService;
import se.magnus.api.event.Event;
import se.magnus.util.exceptions.EventProcessingException;

@EnableBinding(Sink.class)
public class MessageProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(MessageProcessor.class);
    private final NationalTeamService nationalteamService;

    @Autowired
    public MessageProcessor(NationalTeamService nationalteamService) {
        this.nationalteamService = nationalteamService;
    }

    @StreamListener(target = Sink.INPUT)
    public void process(Event<Integer, NationalTeam> event) {
        LOG.info("Process message created at {}...", event.getEventCreatedAt());

        switch (event.getEventType()) {
            case CREATE:
                NationalTeam nationalteam = event.getData();
                LOG.info("Create national team with ID: {}", nationalteam.getNationalTeamId());
                nationalteamService.createNationalTeam(nationalteam);
                break;

            case DELETE:
                int nationalteamId = event.getKey();
                LOG.info("Delete national team with ID: {}", nationalteamId);
                nationalteamService.deleteNationalTeam(nationalteamId);
                break;

            default:
                String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
                LOG.warn(errorMessage);
                throw new EventProcessingException(errorMessage);
        }

        LOG.info("Message processing done!");
    }
}