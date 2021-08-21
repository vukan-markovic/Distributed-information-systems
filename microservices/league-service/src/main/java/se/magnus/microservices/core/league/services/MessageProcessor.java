package se.magnus.microservices.core.league.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import se.magnus.api.core.league.League;
import se.magnus.api.core.league.LeagueService;
import se.magnus.api.event.Event;
import se.magnus.util.exceptions.EventProcessingException;

@EnableBinding(Sink.class)
public class MessageProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(MessageProcessor.class);
    private final LeagueService leagueService;

    @Autowired
    public MessageProcessor(LeagueService leagueService) {
        this.leagueService = leagueService;
    }

    @StreamListener(target = Sink.INPUT)
    public void process(Event<Integer, League> event) {
        LOG.info("Process message created at {}...", event.getEventCreatedAt());

        switch (event.getEventType()) {
            case CREATE:
                League league = event.getData();
                LOG.info("Create league with ID: {}", league.getLeagueId());
                leagueService.createLeague(league);
                break;

            case DELETE:
                int leagueId = event.getKey();
                LOG.info("Delete league with ID: {}", leagueId);
                leagueService.deleteLeague(leagueId);
                break;

            default:
                String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
                LOG.warn(errorMessage);
                throw new EventProcessingException(errorMessage);
        }

        LOG.info("Message processing done!");
    }
}