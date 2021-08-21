package se.magnus.microservices.core.team.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import se.magnus.api.core.team.Team;
import se.magnus.api.core.team.TeamService;
import se.magnus.api.event.Event;
import se.magnus.util.exceptions.EventProcessingException;

@EnableBinding(Sink.class)
public class MessageProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(MessageProcessor.class);
    private final TeamService teamService;

    @Autowired
    public MessageProcessor(TeamService teamService) {
        this.teamService = teamService;
    }

    @StreamListener(target = Sink.INPUT)
    public void process(Event<Integer, Team> event) {
        LOG.info("Process message created at {}...", event.getEventCreatedAt());

        switch (event.getEventType()) {
            case CREATE:
                Team team = event.getData();
                LOG.info("Create team with ID: {}", team.getTeamId());
                teamService.createTeam(team);
                break;

            case DELETE:
                int playerId = event.getKey();
                LOG.info("Delete team with TeamID: {}", playerId);
                teamService.deleteTeam(playerId);
                break;

            default:
                String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
                LOG.warn(errorMessage);
                throw new EventProcessingException(errorMessage);
        }

        LOG.info("Message processing done!");
    }
}