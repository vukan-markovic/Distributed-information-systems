package se.magnus.microservices.core.team.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import se.magnus.api.core.team.Team;
import se.magnus.api.core.team.TeamService;
import se.magnus.microservices.core.team.persistence.TeamEntity;
import se.magnus.microservices.core.team.persistence.TeamRepository;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.http.ServiceUtil;

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

        TeamEntity entity = mapper.apiToEntity(body);
        Mono<Team> newEntity = repository.save(entity)
                .log()
                .onErrorMap(
                        DuplicateKeyException.class,
                        ex -> new InvalidInputException("Duplicate key, Team Id: " + body.getTeamId() + ", Recommendation Id:" + body.getLeagueId()))
                .map(mapper::entityToApi);

        return newEntity.block();
    }

    @Override
    public Mono<Team> getTeam(int teamId) {
        if (teamId < 1) throw new InvalidInputException("Invalid teamId: " + teamId);

        return repository.findByTeamId(teamId)
                .log()
                .map(mapper::entityToApi)
                .map(e -> {
                    e.setServiceAddress(serviceUtil.getServiceAddress());
                    return e;
                });
    }

    @Override
    public void deleteTeam(int teamId) {
        if (teamId < 1) throw new InvalidInputException("Invalid teamId: " + teamId);
        LOG.debug("deleteRecommendations: tries to delete recommendations for the team with teamId: {}", teamId);
        repository.deleteAll(repository.findByTeamId(teamId)).block();
    }
}