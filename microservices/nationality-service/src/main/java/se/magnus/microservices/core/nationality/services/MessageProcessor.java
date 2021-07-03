package se.magnus.microservices.core.nationality.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import se.magnus.api.core.nationality.Nationality;
import se.magnus.api.core.nationality.NationalityService;
import se.magnus.api.event.Event;
import se.magnus.util.exceptions.EventProcessingException;

@EnableBinding(Sink.class)
public class MessageProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(MessageProcessor.class);
    private final NationalityService nationalityService;

    @Autowired
    public MessageProcessor(NationalityService nationalityService) {
        this.nationalityService = nationalityService;
    }

    @StreamListener(target = Sink.INPUT)
    public void process(Event<Integer, Nationality> event) {
        LOG.info("Process message created at {}...", event.getEventCreatedAt());

        switch (event.getEventType()) {
            case CREATE:
                Nationality nationality = event.getData();
                LOG.info("Create nationality with ID: {}", nationality.getNationalityId());
                nationalityService.createNationality(nationality);
                break;

            case DELETE:
                int nationalityId = event.getKey();
                LOG.info("Delete nationality with NationalityID: {}", nationalityId);
                nationalityService.deleteNationality(nationalityId);
                break;

            default:
                String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
                LOG.warn(errorMessage);
                throw new EventProcessingException(errorMessage);
        }

        LOG.info("Message processing done!");
    }
}