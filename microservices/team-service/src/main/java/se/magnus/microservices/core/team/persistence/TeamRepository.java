package se.magnus.microservices.core.team.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface TeamRepository extends ReactiveCrudRepository<TeamEntity, String> {
    Mono<TeamEntity> findByTeamId(int leagueId);
}