package se.magnus.microservices.core.nationality.persistence;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface NationalityRepository extends CrudRepository<NationalityEntity, Integer> {
    Optional<NationalityEntity> findByNationalityId(int nationalityId);
}