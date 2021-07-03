package se.magnus.microservices.core.nationality.services;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import se.magnus.api.core.nationality.Nationality;
import se.magnus.api.core.nationality.NationalityService;
import se.magnus.microservices.core.nationality.persistence.NationalityEntity;
import se.magnus.microservices.core.nationality.persistence.NationalityRepository;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.ServiceUtil;

import java.util.function.Supplier;

import static reactor.core.publisher.Mono.error;

@RestController
public class NationalityServiceImpl implements NationalityService {
    private static final Logger LOG = LoggerFactory.getLogger(NationalityServiceImpl.class);
    private final NationalityRepository repository;
    private final NationalityMapper mapper;
    private final ServiceUtil serviceUtil;
    private final Scheduler scheduler;

    @Autowired
    public NationalityServiceImpl(Scheduler scheduler, NationalityRepository repository, NationalityMapper mapper, ServiceUtil serviceUtil) {
        this.scheduler = scheduler;
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Nationality createNationality(Nationality body) {
        if (body.getNationalityId() < 1)
            throw new InvalidInputException("Invalid nationalityId: " + body.getNationalityId());
        try {
            NationalityEntity entity = mapper.apiToEntity(body);
            NationalityEntity newEntity = repository.save(entity).block();
            LOG.debug("createNationality: created a nationality entity: {}", body.getNationalityId());
            return mapper.entityToApi(newEntity);
        } catch (DataIntegrityViolationException dive) {
            throw new InvalidInputException("Duplicate key, Nationality Id: " + body.getNationalityId());
        }
    }

    @Override
    public Mono<Nationality> getNationality(int nationalityId) {
        if (nationalityId < 1) throw new InvalidInputException("Invalid nationalityId: " + nationalityId);
        LOG.info("Will get nationality for player with id={}", nationalityId);

        return repository.findByNationalityId(nationalityId)
                .switchIfEmpty(error(new NotFoundException("No nationality found for nationalityId: " + nationalityId)))
                .log()
                .map(mapper::entityToApi)
                .map(e -> {e.setServiceAddress(serviceUtil.getServiceAddress()); return e;});
    }

    @Override
    public void deleteNationality(int nationalityId) {
        if (nationalityId < 1) throw new InvalidInputException("Invalid nationalityId: " + nationalityId);
        LOG.debug("deleteNationality: tries to delete nationality with nationalityId: {}", nationalityId);
        repository.findByNationalityId(nationalityId).log().map(repository::delete).flatMap(e -> e).block();
    }

    private <T> Flux<T> asyncFlux(Supplier<Publisher<T>> publisherSupplier) {
        return Flux.defer(publisherSupplier).subscribeOn(scheduler);
    }
}