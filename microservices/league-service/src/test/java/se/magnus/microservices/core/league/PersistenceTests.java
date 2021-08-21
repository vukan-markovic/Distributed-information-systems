package se.magnus.microservices.core.league;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import se.magnus.microservices.core.league.persistence.LeagueEntity;
import se.magnus.microservices.core.league.persistence.LeagueRepository;

import static org.junit.Assert.*;
import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED;

@RunWith(SpringRunner.class)
@DataJpaTest(properties = {"spring.cloud.config.enabled=false"})
@Transactional(propagation = NOT_SUPPORTED)
public class PersistenceTests {
    @Autowired
    private LeagueRepository repository;

    private LeagueEntity savedEntity;

    @Before
    public void setupDb() {
        repository.deleteAll();
        LeagueEntity entity = new LeagueEntity(1, "a", "s");
        savedEntity = repository.save(entity);
        assertEqualsLeague(entity, savedEntity);
    }

    @Test
    public void create() {
        LeagueEntity newEntity = new LeagueEntity(3, "a", "s");
        repository.save(newEntity);
        LeagueEntity foundEntity = repository.findById(newEntity.getId()).get();
        assertEqualsLeague(newEntity, foundEntity);
        assertEquals(2, repository.count());
    }

    @Test
    public void update() {
        savedEntity.setName("a2");
        repository.save(savedEntity);
        LeagueEntity foundEntity = repository.findById(savedEntity.getId()).get();
        assertEquals(1, foundEntity.getVersion());
        assertEquals("a2", foundEntity.getName());
    }

    @Test
    public void delete() {
        repository.delete(savedEntity);
        assertFalse(repository.existsById(savedEntity.getId()));
    }

    @Test
    public void getByLeagueId() {
        LeagueEntity entity = repository.findByLeagueId(savedEntity.getLeagueId());
        assertEqualsLeague(savedEntity, entity);
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void duplicateError() {
        LeagueEntity entity = new LeagueEntity(1, "a", "s");
        repository.save(entity);
    }

    @Test
    public void optimisticLockError() {
        LeagueEntity entity1 = repository.findById(savedEntity.getId()).get();
        LeagueEntity entity2 = repository.findById(savedEntity.getId()).get();
        entity1.setName("a1");
        repository.save(entity1);

        try {
            entity2.setName("a2");
            repository.save(entity2);
            fail("Expected an OptimisticLockingFailureException");
        } catch (OptimisticLockingFailureException ignored) {
        }

        LeagueEntity updatedEntity = repository.findById(savedEntity.getId()).get();
        assertEquals(1, updatedEntity.getVersion());
        assertEquals("a1", updatedEntity.getName());
    }

    private void assertEqualsLeague(LeagueEntity expectedEntity, LeagueEntity actualEntity) {
        assertEquals(expectedEntity.getId(), actualEntity.getId());
        assertEquals(expectedEntity.getVersion(), actualEntity.getVersion());
        assertEquals(expectedEntity.getLeagueId(), actualEntity.getLeagueId());
        assertEquals(expectedEntity.getName(), actualEntity.getName());
        assertEquals(expectedEntity.getLabel(), actualEntity.getLabel());
    }
}