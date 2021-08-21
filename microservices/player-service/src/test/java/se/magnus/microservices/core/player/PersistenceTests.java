package se.magnus.microservices.core.player;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.test.StepVerifier;
import se.magnus.microservices.core.player.persistence.PlayerEntity;
import se.magnus.microservices.core.player.persistence.PlayerRepository;

@RunWith(SpringRunner.class)
@DataMongoTest(properties = {"spring.cloud.config.enabled=false"})
public class PersistenceTests {

    @Autowired
    private PlayerRepository repository;

    private PlayerEntity savedEntity;

    @Before
    public void setupDb() {
        StepVerifier.create(repository.deleteAll()).verifyComplete();
        PlayerEntity entity = new PlayerEntity(1, "n", "c", "d", "02.02.2021.", 1, 1, 1, 1);

        StepVerifier.create(repository.save(entity))
                .expectNextMatches(createdEntity -> {
                    savedEntity = createdEntity;
                    return arePlayerEqual(entity, savedEntity);
                }).verifyComplete();
    }

    @Test
    public void create() {
        PlayerEntity newEntity = new PlayerEntity(2, "n", "c", "d", "02.02.2021.", 1, 1, 1, 1);

        StepVerifier.create(repository.save(newEntity))
                .expectNextMatches(createdEntity -> newEntity.getPlayerId() == createdEntity.getPlayerId())
                .verifyComplete();

        StepVerifier.create(repository.findById(newEntity.getId()))
                .expectNextMatches(foundEntity -> arePlayerEqual(newEntity, foundEntity))
                .verifyComplete();

        StepVerifier.create(repository.count()).expectNext(2L).verifyComplete();
    }

    @Test
    public void update() {
        savedEntity.setName("n2");

        StepVerifier.create(repository.save(savedEntity))
                .expectNextMatches(updatedEntity -> updatedEntity.getName().equals("n2"))
                .verifyComplete();

        StepVerifier.create(repository.findById(savedEntity.getId()))
                .expectNextMatches(foundEntity ->
                        foundEntity.getVersion() == 1 &&
                                foundEntity.getName().equals("n2"))
                .verifyComplete();
    }

    @Test
    public void delete() {
        StepVerifier.create(repository.delete(savedEntity)).verifyComplete();
        StepVerifier.create(repository.existsById(savedEntity.getId())).expectNext(false).verifyComplete();
    }

    @Test
    public void getByPlayerId() {
        StepVerifier.create(repository.findByPlayerId(savedEntity.getPlayerId()))
                .expectNextMatches(foundEntity -> arePlayerEqual(savedEntity, foundEntity))
                .verifyComplete();
    }

    @Test
    public void duplicateError() {
        PlayerEntity entity = new PlayerEntity(1, "n", "c", "d", "02.02.2021.", 1, 1, 1, 1);
        StepVerifier.create(repository.save(entity)).expectError(DuplicateKeyException.class).verify();
    }

    @Test
    public void optimisticLockError() {
        PlayerEntity entity1 = repository.findById(savedEntity.getId()).block();
        PlayerEntity entity2 = repository.findById(savedEntity.getId()).block();
        entity1.setName("n1");
        repository.save(entity1).block();
        StepVerifier.create(repository.save(entity2)).expectError(OptimisticLockingFailureException.class).verify();

        StepVerifier.create(repository.findById(savedEntity.getId()))
                .expectNextMatches(foundEntity ->
                        foundEntity.getVersion() == 1 &&
                                foundEntity.getName().equals("n1"))
                .verifyComplete();
    }

    private boolean arePlayerEqual(PlayerEntity expectedEntity, PlayerEntity actualEntity) {
        return
                (expectedEntity.getId().equals(actualEntity.getId())) &&
                        (expectedEntity.getVersion().equals(actualEntity.getVersion())) &&
                        (expectedEntity.getPlayerId() == actualEntity.getPlayerId()) &&
                        (expectedEntity.getName().equals(actualEntity.getName())) &&
                        (expectedEntity.getSurname().equals(actualEntity.getSurname())) &&
                        (expectedEntity.getRegistrationNumber().equals(actualEntity.getRegistrationNumber())) &&
                        (expectedEntity.getDateOfBirth().equals(actualEntity.getDateOfBirth()));
    }
}