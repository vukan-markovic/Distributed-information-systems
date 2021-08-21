package se.magnus.microservices.core.nationalteam.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface NationalTeamRepository extends ReactiveCrudRepository<NationalTeamEntity, String> {
    Mono<NationalTeamEntity> findByNationalteamId(int nationalteamId);
}