package se.magnus.microservices.core.nationalTeam.services;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import se.magnus.api.core.nationalTeam.NationalTeam;
import se.magnus.api.core.nationalTeam.NationalTeamService;
import se.magnus.microservices.core.nationalTeam.persistence.NationalTeamRepository;
import se.magnus.microservices.core.nationalTeam.persistence.NationalTeamEntity;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.ServiceUtil;

import java.util.function.Supplier;

import static reactor.core.publisher.Mono.error;

@RestController
class NationalTeamServiceImpl implements NationalTeamService {
    private static final Logger LOG = LoggerFactory.getLogger(NationalTeamServiceImpl.class);
    private final NationalTeamRepository repository;
    private final NationalTeamMapper mapper;
    private final ServiceUtil serviceUtil;
    private final Scheduler scheduler;

    @Autowired
    public NationalTeamServiceImpl(Scheduler scheduler, NationalTeamRepository repository, NationalTeamMapper mapper, ServiceUtil serviceUtil) {
        this.scheduler = scheduler;
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public NationalTeam createNationalTeam(NationalTeam body) {
        if (body.getNationalTeamId() < 1)
            throw new InvalidInputException("Invalid nationalTeamId: " + body.getNationalTeamId());
        try {
            NationalTeamEntity entity = mapper.apiToEntity(body);
            NationalTeamEntity newEntity = repository.save(entity).block();
            LOG.debug("createReview: created a nationalTeam entity: {}", body.getNationalTeamId());
            return mapper.entityToApi(newEntity);
        } catch (DataIntegrityViolationException dive) {
            throw new InvalidInputException("Duplicate key, NationalTeam Id: " + body.getNationalTeamId());
        }
    }

    @Override
    public Mono<NationalTeam> getNationalTeam(int nationalTeamId) {
        if (nationalTeamId < 1) throw new InvalidInputException("Invalid nationalTeamId: " + nationalTeamId);
        LOG.info("Will get national team for player with id={}", nationalTeamId);

        return repository.findByNationalTeamId(nationalTeamId)
                .switchIfEmpty(error(new NotFoundException("No nationality found for nationalTeamId: " + nationalTeamId)))
                .log()
                .map(mapper::entityToApi)
                .map(e -> {e.setServiceAddress(serviceUtil.getServiceAddress()); return e;});
    }

    @Override
    public void deleteNationalTeam(int nationalTeamId) {
        if (nationalTeamId < 1) throw new InvalidInputException("Invalid playerId: " + nationalTeamId);
        LOG.debug("deleteReviews: tries to delete reviews for the player with playerId: {}", nationalTeamId);
        repository.deleteAll(repository.findByNationalTeamId(nationalTeamId));
    }

    private <T> Flux<T> asyncFlux(Supplier<Publisher<T>> publisherSupplier) {
        return Flux.defer(publisherSupplier).subscribeOn(scheduler);
    }
}