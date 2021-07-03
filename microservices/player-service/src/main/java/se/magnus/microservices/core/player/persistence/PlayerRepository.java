package se.magnus.microservices.core.player.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface PlayerRepository extends ReactiveCrudRepository<PlayerEntity, String> {
    Mono<PlayerEntity> findByPlayerId(int playerId);
}