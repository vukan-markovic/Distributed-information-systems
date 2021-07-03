package se.magnus.microservices.core.nationalTeam.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface NationalTeamRepository extends ReactiveCrudRepository<NationalTeamEntity, String> {
    Mono<NationalTeamEntity> findByNationalTeamId(int nationalTeamId);
}