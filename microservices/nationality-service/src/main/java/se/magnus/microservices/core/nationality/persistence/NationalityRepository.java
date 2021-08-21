package se.magnus.microservices.core.nationality.persistence;

import org.springframework.data.repository.CrudRepository;

public interface NationalityRepository extends CrudRepository<NationalityEntity, Integer> {
    NationalityEntity findByNationalityId(int nationalityId);
}