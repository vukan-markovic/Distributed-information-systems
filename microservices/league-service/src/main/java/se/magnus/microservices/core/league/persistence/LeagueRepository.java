package se.magnus.microservices.core.league.persistence;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface LeagueRepository extends CrudRepository<LeagueEntity, Integer> {
    Optional<LeagueEntity> findByLeagueId(int leagueId);
}