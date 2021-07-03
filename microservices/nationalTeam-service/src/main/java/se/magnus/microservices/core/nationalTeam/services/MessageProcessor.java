package se.magnus.microservices.core.nationalTeam.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import se.magnus.api.core.nationalTeam.NationalTeam;
import se.magnus.api.core.nationalTeam.NationalTeamService;
import se.magnus.api.event.Event;
import se.magnus.util.exceptions.EventProcessingException;

@EnableBinding(Sink.class)
public class MessageProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(MessageProcessor.class);
    private final NationalTeamService nationalTeamService;

    @Autowired
    public MessageProcessor(NationalTeamService nationalTeamService) {
        this.nationalTeamService = nationalTeamService;
    }

    @StreamListener(target = Sink.INPUT)
    public void process(Event<Integer, NationalTeam> event) {
        LOG.info("Process message created at {}...", event.getEventCreatedAt());

        switch (event.getEventType()) {
            case CREATE:
                NationalTeam nationalTeam = event.getData();
                LOG.info("Create national team with ID: {}", nationalTeam.getNationalTeamId());
                nationalTeamService.createNationalTeam(nationalTeam);
                break;

            case DELETE:
                int nationalTeamId = event.getKey();
                LOG.info("Delete national team with NationalTeamID: {}", nationalTeamId);
                nationalTeamService.deleteNationalTeam(nationalTeamId);
                break;

            default:
                String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
                LOG.warn(errorMessage);
                throw new EventProcessingException(errorMessage);
        }

        LOG.info("Message processing done!");
    }
}