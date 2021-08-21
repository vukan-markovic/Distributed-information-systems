package se.magnus.microservices.core.league.persistence;

import org.springframework.data.repository.CrudRepository;

public interface LeagueRepository extends CrudRepository<LeagueEntity, Integer> {
    LeagueEntity findByLeagueId(int leagueId);
}