package se.magnus.microservices.core.nationalteam.persistence;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface NationalTeamRepository extends CrudRepository<NationalTeamEntity, Integer> {
    Optional<NationalTeamEntity> findByNationalteamId(int nationalteamId);
}