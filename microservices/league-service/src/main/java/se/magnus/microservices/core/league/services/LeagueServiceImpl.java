package se.magnus.microservices.core.league.services;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import se.magnus.api.core.league.League;
import se.magnus.api.core.league.LeagueService;
import se.magnus.microservices.core.league.persistence.LeagueEntity;
import se.magnus.microservices.core.league.persistence.LeagueRepository;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.http.ServiceUtil;

import java.util.function.Supplier;

@RestController
public class LeagueServiceImpl implements LeagueService {
    private static final Logger LOG = LoggerFactory.getLogger(LeagueServiceImpl.class);
    private final LeagueRepository repository;
    private final LeagueMapper mapper;
    private final ServiceUtil serviceUtil;
    private final Scheduler scheduler;

    @Autowired
    public LeagueServiceImpl(Scheduler scheduler, LeagueRepository repository, LeagueMapper mapper, ServiceUtil serviceUtil) {
        this.scheduler = scheduler;
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public League createLeague(League body) {
        if (body.getLeagueId() < 1)
            throw new InvalidInputException("Invalid leagueId: " + body.getLeagueId());

        try {
            LeagueEntity entity = mapper.apiToEntity(body);
            LeagueEntity newEntity = repository.save(entity);
            LOG.debug("createLeague: created a league entity: {}", body.getLeagueId());
            return mapper.entityToApi(newEntity);
        } catch (DataIntegrityViolationException dive) {
            throw new InvalidInputException("Duplicate key, League Id: " + body.getLeagueId());
        }
    }

    @Override
    public Mono<League> getLeague(int leagueId) {
        if (leagueId < 1) throw new InvalidInputException("Invalid leagueId: " + leagueId);
        LOG.info("Will get league with id={}", leagueId);
        return Mono.just(getByLeagueId(leagueId));
    }

    protected League getByLeagueId(int leagueId) {
        LeagueEntity entity = repository.findByLeagueId(leagueId);
        League api = mapper.entityToApi(entity);
        api.setServiceAddress(serviceUtil.getServiceAddress());
        LOG.debug("getLeague");
        return api;
    }

    @Override
    public void deleteLeague(int leagueId) {
        if (leagueId < 1) throw new InvalidInputException("Invalid leagueId: " + leagueId);
        LOG.debug("deleteLeague: tries to delete league with leagueId: {}", leagueId);
        repository.delete(repository.findByLeagueId(leagueId));
    }

    private <T> Flux<T> asyncFlux(Supplier<Publisher<T>> publisherSupplier) {
        return Flux.defer(publisherSupplier).subscribeOn(scheduler);
    }
}