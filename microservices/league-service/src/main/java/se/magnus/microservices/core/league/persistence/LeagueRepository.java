package se.magnus.microservices.core.league.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface LeagueRepository extends ReactiveCrudRepository<LeagueEntity, String> {
    Mono<LeagueEntity> findByLeagueId(int leagueId);
}