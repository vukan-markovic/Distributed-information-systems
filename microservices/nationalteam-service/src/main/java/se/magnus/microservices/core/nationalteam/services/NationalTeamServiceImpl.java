package se.magnus.microservices.core.nationalteam.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;
import se.magnus.api.core.nationalteam.NationalTeam;
import se.magnus.api.core.nationalteam.NationalTeamService;
import se.magnus.microservices.core.nationalteam.persistence.NationalTeamEntity;
import se.magnus.microservices.core.nationalteam.persistence.NationalTeamRepository;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.ServiceUtil;

@SuppressWarnings("ALL")
@RestController
class NationalTeamServiceImpl implements NationalTeamService {
    private static final Logger LOG = LoggerFactory.getLogger(NationalTeamServiceImpl.class);
    private final NationalTeamRepository repository;
    private final NationalTeamMapper mapper;
    private final ServiceUtil serviceUtil;

    @Autowired
    public NationalTeamServiceImpl(NationalTeamRepository repository, NationalTeamMapper mapper, ServiceUtil serviceUtil) {
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public NationalTeam createNationalTeam(NationalTeam body) {
        if (body.getNationalTeamId() < 1)
            throw new InvalidInputException("Invalid nationalteamId: " + body.getNationalTeamId());

        try {
            NationalTeamEntity entity = mapper.apiToEntity(body);
            NationalTeamEntity newEntity = repository.save(entity);
            LOG.debug("createNationalTeam: created a national team entity: {}", body.getNationalTeamId());
            return mapper.entityToApi(newEntity);
        } catch (DataIntegrityViolationException dive) {
            throw new InvalidInputException("Duplicate key, National team Id: " + body.getNationalTeamId());
        }
    }

    @Override
    public NationalTeam getNationalTeam(int nationalteamId) {
        if (nationalteamId < 1) throw new InvalidInputException("Invalid nationalteamId: " + nationalteamId);
        NationalTeamEntity entity = repository.findByNationalteamId(nationalteamId).orElseThrow(() -> new NotFoundException("No national team found for nationalteamId: " + nationalteamId));
        NationalTeam api = mapper.entityToApi(entity);
        api.setServiceAddress(serviceUtil.getServiceAddress());
        LOG.debug("getNationalTeam");
        return api;
    }

    @Override
    public void deleteNationalTeam(int nationalteamId) {
        if (nationalteamId < 1) throw new InvalidInputException("Invalid nationalteamId: " + nationalteamId);
        LOG.debug("deleteNationalTeam: tries to delete national team with nationalteamId: {}", nationalteamId);
        repository.delete(repository.findByNationalteamId(nationalteamId).get());
    }
}