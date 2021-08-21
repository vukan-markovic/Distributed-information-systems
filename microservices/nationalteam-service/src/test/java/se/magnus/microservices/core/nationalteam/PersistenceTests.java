//package se.magnus.microservices.core.nationalteam;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
//import org.springframework.dao.DataIntegrityViolationException;
//import org.springframework.dao.DuplicateKeyException;
//import org.springframework.dao.OptimisticLockingFailureException;
//import org.springframework.test.context.junit4.SpringRunner;
//import reactor.test.StepVerifier;
//import se.magnus.microservices.core.nationalteam.persistence.NationalTeamEntity;
//import se.magnus.microservices.core.nationalteam.persistence.NationalTeamRepository;
//
//@RunWith(SpringRunner.class)
//@DataMongoTest(properties = {"spring.cloud.config.enabled=false"})
//public class PersistenceTests {
//    @Autowired
//    private NationalTeamRepository repository;
//
//    private NationalTeamEntity savedEntity;
//
//    @Before
//    public void setupDb() {
//        StepVerifier.create(repository.deleteAll()).verifyComplete();
//        NationalTeamEntity entity = new NationalTeamEntity(1, "a", "s");
//
//        StepVerifier.create(repository.save(entity))
//                .expectNextMatches(createdEntity -> {
//                    savedEntity = createdEntity;
//                    return areNationalTeamEqual(entity, savedEntity);
//                })
//                .verifyComplete();
//    }
//
//
//    @Test
//    public void create() {
//        NationalTeamEntity newEntity = new NationalTeamEntity(3, "a", "s");
//
//        StepVerifier.create(repository.save(newEntity))
//                .expectNextMatches(createdEntity -> newEntity.getNationalTeamId() == createdEntity.getNationalTeamId())
//                .verifyComplete();
//
//        StepVerifier.create(repository.findById(newEntity.getId()))
//                .expectNextMatches(foundEntity -> areNationalTeamEqual(newEntity, foundEntity))
//                .verifyComplete();
//
//        StepVerifier.create(repository.count()).expectNext(2L).verifyComplete();
//    }
//
//    @Test
//    public void update() {
//        savedEntity.setName("a2");
//
//        StepVerifier.create(repository.save(savedEntity))
//                .expectNextMatches(updatedEntity -> updatedEntity.getName().equals("n2"))
//                .verifyComplete();
//
//        StepVerifier.create(repository.findById(savedEntity.getId()))
//                .expectNextMatches(foundEntity ->
//                        foundEntity.getVersion() == 1 &&
//                                foundEntity.getName().equals("n2"))
//                .verifyComplete();
//    }
//
//    @Test
//    public void delete() {
//        StepVerifier.create(repository.delete(savedEntity)).verifyComplete();
//        StepVerifier.create(repository.existsById(savedEntity.getId())).expectNext(false).verifyComplete();
//    }
//
//    @Test
//    public void getByNationalTeamId() {
//        StepVerifier.create(repository.findByNationalteamId(savedEntity.getNationalTeamId()))
//                .expectNextMatches(foundEntity -> areNationalTeamEqual(savedEntity, foundEntity))
//                .verifyComplete();
//    }
//
//    @Test(expected = DataIntegrityViolationException.class)
//    public void duplicateError() {
//        NationalTeamEntity entity = new NationalTeamEntity(1, "a", "s");
//        StepVerifier.create(repository.save(entity)).expectError(DuplicateKeyException.class).verify();
//    }
//
//    @Test
//    public void optimisticLockError() {
//        NationalTeamEntity entity1 = repository.findById(savedEntity.getId()).block();
//        NationalTeamEntity entity2 = repository.findById(savedEntity.getId()).block();
//        entity1.setName("a1");
//        repository.save(entity1).block();
//        StepVerifier.create(repository.save(entity2)).expectError(OptimisticLockingFailureException.class).verify();
//
//        StepVerifier.create(repository.findById(savedEntity.getId()))
//                .expectNextMatches(foundEntity ->
//                        foundEntity.getVersion() == 1 &&
//                                foundEntity.getName().equals("n1"))
//                .verifyComplete();
//    }
//
//    private boolean areNationalTeamEqual(NationalTeamEntity expectedEntity, NationalTeamEntity actualEntity) {
//        return
//                (expectedEntity.getId().equals(actualEntity.getId())) &&
//                        (expectedEntity.getVersion() == actualEntity.getVersion()) &&
//                        (expectedEntity.getNationalTeamId() == actualEntity.getNationalTeamId()) &&
//                        (expectedEntity.getName().equals(actualEntity.getName())) &&
//                        (expectedEntity.getTeamSelector().equals(actualEntity.getTeamSelector()));
//    }
//}