package se.magnus.microservices.core.nationality.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.scheduler.Scheduler;
import se.magnus.api.core.nationality.Nationality;
import se.magnus.api.core.nationality.NationalityService;
import se.magnus.microservices.core.nationality.persistence.NationalityEntity;
import se.magnus.microservices.core.nationality.persistence.NationalityRepository;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.ServiceUtil;

@SuppressWarnings("ALL")
@RestController
public class NationalityServiceImpl implements NationalityService {
    private static final Logger LOG = LoggerFactory.getLogger(NationalityServiceImpl.class);
    private final NationalityRepository repository;
    private final NationalityMapper mapper;
    private final ServiceUtil serviceUtil;

    @Autowired
    public NationalityServiceImpl(NationalityRepository repository, NationalityMapper mapper, ServiceUtil serviceUtil) {
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
    public Nationality getNationality(int nationalityId) {
        if (nationalityId < 1) throw new InvalidInputException("Invalid nationalityId: " + nationalityId);
        NationalityEntity entity = repository.findByNationalityId(nationalityId).orElseThrow(() -> new NotFoundException("No nationality found for nationalityId: " + nationalityId));;
        Nationality api = mapper.entityToApi(entity);
        api.setServiceAddress(serviceUtil.getServiceAddress());
        LOG.debug("getNationality");
        return api;
    }

    @Override
    public void deleteNationality(int nationalityId) {
        if (nationalityId < 1) throw new InvalidInputException("Invalid nationalityId: " + nationalityId);
        LOG.debug("deleteNationality: tries to delete nationality with nationalityId: {}", nationalityId);
        repository.delete(repository.findByNationalityId(nationalityId).get());
    }
}