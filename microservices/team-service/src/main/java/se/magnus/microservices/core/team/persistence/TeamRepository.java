package se.magnus.microservices.core.team.persistence;

import org.springframework.data.repository.CrudRepository;

public interface TeamRepository extends CrudRepository<TeamEntity, Integer> {
    TeamEntity findByTeamId(int leagueId);
}