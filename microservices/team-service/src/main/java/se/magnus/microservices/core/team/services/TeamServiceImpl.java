package se.magnus.microservices.core.team.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;
import se.magnus.api.core.team.Team;
import se.magnus.api.core.team.TeamService;
import se.magnus.microservices.core.team.persistence.TeamEntity;
import se.magnus.microservices.core.team.persistence.TeamRepository;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.ServiceUtil;

@SuppressWarnings("ALL")
@RestController
public class TeamServiceImpl implements TeamService {
    private static final Logger LOG = LoggerFactory.getLogger(TeamServiceImpl.class);
    private final TeamRepository repository;
    private final TeamMapper mapper;
    private final ServiceUtil serviceUtil;

    @Autowired
    public TeamServiceImpl(TeamRepository repository, TeamMapper mapper, ServiceUtil serviceUtil) {
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Team createTeam(Team body) {
        if (body.getTeamId() < 1) throw new InvalidInputException("Invalid teamId: " + body.getTeamId());

        try {
            TeamEntity entity = mapper.apiToEntity(body);
            TeamEntity newEntity = repository.save(entity);
            LOG.debug("createTeam: created a team entity: {}", body.getTeamId());
            return mapper.entityToApi(newEntity);
        } catch (DataIntegrityViolationException dive) {
            throw new InvalidInputException("Duplicate key, Team Id: " + body.getTeamId());
        }
    }

    @Override
    public Team getTeam(int teamId) {
        if (teamId < 1) throw new InvalidInputException("Invalid teamId: " + teamId);
        TeamEntity entity = repository.findByTeamId(teamId).orElseThrow(() -> new NotFoundException("No team found for teamId: " + teamId));
        Team api = mapper.entityToApi(entity);
        api.setServiceAddress(serviceUtil.getServiceAddress());
        LOG.debug("getTeam");
        return api;
    }

    @Override
    public void deleteTeam(int teamId) {
        if (teamId < 1) throw new InvalidInputException("Invalid teamId: " + teamId);
        LOG.debug("deleteTeam: tries to delete team with teamId: {}", teamId);
        repository.delete(repository.findByTeamId(teamId).get());
    }
}