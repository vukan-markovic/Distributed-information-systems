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
import se.magnus.util.http.ServiceUtil;

import java.util.function.Supplier;

@SuppressWarnings("ALL")
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
            NationalityEntity newEntity = repository.save(entity);
            LOG.debug("createNationality: created a nationality entity: {}", body.getNationalityId());
            return mapper.entityToApi(newEntity);
        } catch (DataIntegrityViolationException dive) {
            throw new InvalidInputException("Duplicate key, Nationality Id: " + body.getNationalityId());
        }
    }

    @Override
    public Mono<Nationality> getNationality(int nationalityId) {
        if (nationalityId < 1) throw new InvalidInputException("Invalid nationalityId: " + nationalityId);
        LOG.info("Will get nationality with id={}", nationalityId);
        return Mono.just(getByNationalityId(nationalityId));
    }

    protected Nationality getByNationalityId(int nationalityId) {
        NationalityEntity entity = repository.findByNationalityId(nationalityId);
        Nationality api = mapper.entityToApi(entity);
        api.setServiceAddress(serviceUtil.getServiceAddress());
        LOG.debug("getNationality");
        return api;
    }

    @Override
    public void deleteNationality(int nationalityId) {
        if (nationalityId < 1) throw new InvalidInputException("Invalid nationalityId: " + nationalityId);
        LOG.debug("deleteNationality: tries to delete nationality with nationalityId: {}", nationalityId);
        repository.delete(repository.findByNationalityId(nationalityId));
    }

    private <T> Flux<T> asyncFlux(Supplier<Publisher<T>> publisherSupplier) {
        return Flux.defer(publisherSupplier).subscribeOn(scheduler);
    }
}