package se.magnus.microservices.core.player;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import se.magnus.microservices.core.player.persistence.PlayerEntity;
import se.magnus.microservices.core.player.persistence.PlayerRepository;

import static org.junit.Assert.*;
import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED;

@RunWith(SpringRunner.class)
@DataJpaTest(properties = {"spring.cloud.config.enabled=false"})
@Transactional(propagation = NOT_SUPPORTED)
public class PersistenceTests {
    @Autowired
    private PlayerRepository repository;

    private PlayerEntity savedEntity;

    @Before
    public void setupDb() {
        repository.deleteAll();
        PlayerEntity entity = new PlayerEntity(1, "n", "c", "d", "02.02.2021.", 1, 1, 1, 1);
        savedEntity = repository.save(entity);
        assertEqualsPlayer(entity, savedEntity);
    }

    @Test
    public void create() {
        PlayerEntity newEntity = new PlayerEntity(2, "n", "c", "d", "02.02.2021.", 1, 1, 1, 1);
        repository.save(newEntity);
        PlayerEntity foundEntity = repository.findById(newEntity.getId()).get();
        assertEqualsPlayer(newEntity, foundEntity);
        assertEquals(2, repository.count());
    }

    @Test
    public void update() {
        savedEntity.setName("a2");
        repository.save(savedEntity);
        PlayerEntity foundEntity = repository.findById(savedEntity.getId()).get();
        assertEquals(1, foundEntity.getVersion());
        assertEquals("a2", foundEntity.getName());
    }

    @Test
    public void delete() {
        repository.delete(savedEntity);
        assertFalse(repository.existsById(savedEntity.getId()));
    }

    @Test
    public void getByPlayerId() {
        PlayerEntity entity = repository.findByPlayerId(savedEntity.getLeagueId()).get();
        assertEqualsPlayer(savedEntity, entity);
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void duplicateError() {
        PlayerEntity entity = new PlayerEntity(1, "n", "c", "d", "02.02.2021.", 1, 1, 1, 1);
        repository.save(entity);
    }

    @Test
    public void optimisticLockError() {
        PlayerEntity entity1 = repository.findById(savedEntity.getId()).get();
        PlayerEntity entity2 = repository.findById(savedEntity.getId()).get();
        entity1.setName("a1");
        repository.save(entity1);

        try {
            entity2.setName("a2");
            repository.save(entity2);
            fail("Expected an OptimisticLockingFailureException");
        } catch (OptimisticLockingFailureException ignored) {
        }

        PlayerEntity updatedEntity = repository.findById(savedEntity.getId()).get();
        assertEquals(1, updatedEntity.getVersion());
        assertEquals("a1", updatedEntity.getName());
    }

    private void assertEqualsPlayer(PlayerEntity expectedEntity, PlayerEntity actualEntity) {
        assertEquals(expectedEntity.getId(), actualEntity.getId());
        assertEquals(expectedEntity.getVersion(), actualEntity.getVersion());
        assertEquals(expectedEntity.getPlayerId(), actualEntity.getPlayerId());
        assertEquals(expectedEntity.getName(), actualEntity.getName());
        assertEquals(expectedEntity.getDateOfBirth(), actualEntity.getDateOfBirth());
        assertEquals(expectedEntity.getSurname(), actualEntity.getSurname());
        assertEquals(expectedEntity.getRegistrationNumber(), actualEntity.getRegistrationNumber());
        assertEquals(expectedEntity.getLeagueId(), actualEntity.getLeagueId());
        assertEquals(expectedEntity.getNationalityId(), actualEntity.getNationalityId());
        assertEquals(expectedEntity.getTeamId(), actualEntity.getTeamId());
        assertEquals(expectedEntity.getNationalTeamId(), actualEntity.getNationalTeamId());
    }
}