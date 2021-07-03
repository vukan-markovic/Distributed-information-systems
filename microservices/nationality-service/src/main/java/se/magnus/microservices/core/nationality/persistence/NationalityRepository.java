package se.magnus.microservices.core.nationality.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface NationalityRepository extends ReactiveCrudRepository<NationalityEntity, String> {
    Mono<NationalityEntity> findByNationalityId(int nationalityId);
}