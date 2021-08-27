package se.magnus.microservices.core.nationality;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import se.magnus.microservices.core.nationality.persistence.NationalityEntity;
import se.magnus.microservices.core.nationality.persistence.NationalityRepository;

import static org.junit.Assert.*;
import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED;

@RunWith(SpringRunner.class)
@DataJpaTest(properties = {"spring.cloud.config.enabled=false"})
@Transactional(propagation = NOT_SUPPORTED)
public class PersistenceTests {
    @Autowired
    private NationalityRepository repository;

    private NationalityEntity savedEntity;

    @Before
    public void setupDb() {
        repository.deleteAll();
        NationalityEntity entity = new NationalityEntity(1, 2, "a", "s");
        savedEntity = repository.save(entity);
        assertEqualsNationality(entity, savedEntity);
    }

    @Test
    public void create() {
        NationalityEntity newEntity = new NationalityEntity(2, 3, "a", "s");
        repository.save(newEntity);
        NationalityEntity foundEntity = repository.findById(newEntity.getId()).get();
        assertEqualsNationality(newEntity, foundEntity);
        assertEquals(2, repository.count());
    }

    @Test
    public void update() {
        savedEntity.setName("a2");
        repository.save(savedEntity);
        NationalityEntity foundEntity = repository.findById(savedEntity.getId()).get();
        assertEquals(1, foundEntity.getVersion());
        assertEquals("a2", foundEntity.getName());
    }

    @Test
    public void delete() {
        repository.delete(savedEntity);
        assertFalse(repository.existsById(savedEntity.getId()));
    }

    @Test
    public void getByNationalityId() {
        NationalityEntity entity = repository.findByNationalityId(savedEntity.getNationalityId()).get();
        assertEqualsNationality(savedEntity, entity);
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void duplicateError() {
        NationalityEntity entity = new NationalityEntity(1, 2, "a", "s");
        repository.save(entity);
    }

    @Test
    public void optimisticLockError() {
        NationalityEntity entity1 = repository.findById(savedEntity.getId()).get();
        NationalityEntity entity2 = repository.findById(savedEntity.getId()).get();
        entity1.setName("a1");
        repository.save(entity1);

        try {
            entity2.setName("a2");
            repository.save(entity2);
            fail("Expected an OptimisticLockingFailureException");
        } catch (OptimisticLockingFailureException ignored) {
        }

        NationalityEntity updatedEntity = repository.findById(savedEntity.getId()).get();
        assertEquals(1, updatedEntity.getVersion());
        assertEquals("a1", updatedEntity.getName());
    }

    private void assertEqualsNationality(NationalityEntity expectedEntity, NationalityEntity actualEntity) {
        assertEquals(expectedEntity.getId(), actualEntity.getId());
        assertEquals(expectedEntity.getVersion(), actualEntity.getVersion());
        assertEquals(expectedEntity.getNationalityId(), actualEntity.getNationalityId());
        assertEquals(expectedEntity.getName(), actualEntity.getName());
        assertEquals(expectedEntity.getAbbreviation(), actualEntity.getAbbreviation());
    }
}