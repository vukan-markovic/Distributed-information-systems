package se.magnus.microservices.core.nationalteam;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import se.magnus.microservices.core.nationalteam.persistence.NationalTeamEntity;
import se.magnus.microservices.core.nationalteam.persistence.NationalTeamRepository;

import static org.junit.Assert.*;
import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED;

@RunWith(SpringRunner.class)
@DataJpaTest(properties = {"spring.cloud.config.enabled=false"})
@Transactional(propagation = NOT_SUPPORTED)
public class PersistenceTests {
    @Autowired
    private NationalTeamRepository repository;

    private NationalTeamEntity savedEntity;

    @Before
    public void setupDb() {
        repository.deleteAll();
        NationalTeamEntity entity = new NationalTeamEntity(1, "a", "s");
        savedEntity = repository.save(entity);
        assertEqualsNationalTeam(entity, savedEntity);
    }

    @Test
    public void create() {
        NationalTeamEntity newEntity = new NationalTeamEntity(3, "a", "s");
        repository.save(newEntity);
        NationalTeamEntity foundEntity = repository.findByNationalteamId(newEntity.getId()).get();
        assertEqualsNationalTeam(newEntity, foundEntity);
        assertEquals(2, repository.count());
    }

    @Test
    public void update() {
        savedEntity.setName("a2");
        repository.save(savedEntity);
        NationalTeamEntity foundEntity = repository.findById(savedEntity.getId()).get();
        assertEquals(1, foundEntity.getVersion());
        assertEquals("a2", foundEntity.getName());
    }

    @Test
    public void delete() {
        repository.delete(savedEntity);
        assertFalse(repository.existsById(savedEntity.getId()));
    }

    @Test
    public void getByNationalTeamId() {
        NationalTeamEntity entity = repository.findByNationalteamId(savedEntity.getNationalTeamId()).get();
        assertEqualsNationalTeam(savedEntity, entity);
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void duplicateError() {
        NationalTeamEntity entity = new NationalTeamEntity(1, "a", "s");
        repository.save(entity);
    }

    @Test
    public void optimisticLockError() {
        NationalTeamEntity entity1 = repository.findById(savedEntity.getId()).get();
        NationalTeamEntity entity2 = repository.findById(savedEntity.getId()).get();
        entity1.setName("a1");
        repository.save(entity1);

        try {
            entity2.setName("a2");
            repository.save(entity2);
            fail("Expected an OptimisticLockingFailureException");
        } catch (OptimisticLockingFailureException ignored) {
        }

        NationalTeamEntity updatedEntity = repository.findById(savedEntity.getId()).get();
        assertEquals(1, updatedEntity.getVersion());
        assertEquals("a1", updatedEntity.getName());
    }

    private void assertEqualsNationalTeam(NationalTeamEntity expectedEntity, NationalTeamEntity actualEntity) {
        assertEquals(expectedEntity.getId(), actualEntity.getId());
        assertEquals(expectedEntity.getVersion(), actualEntity.getVersion());
        assertEquals(expectedEntity.getNationalTeamId(), actualEntity.getNationalTeamId());
        assertEquals(expectedEntity.getName(), actualEntity.getName());
        assertEquals(expectedEntity.getTeamSelector(), actualEntity.getTeamSelector());
    }
}