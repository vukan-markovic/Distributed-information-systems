package se.magnus.microservices.core.team.persistence;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface TeamRepository extends CrudRepository<TeamEntity, Integer> {
    Optional<TeamEntity> findByTeamId(int leagueId);
}