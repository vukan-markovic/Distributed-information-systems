package se.magnus.microservices.core.player.persistence;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface PlayerRepository extends CrudRepository<PlayerEntity, Integer> {
    Optional<PlayerEntity> findByPlayerId(int playerId);
}