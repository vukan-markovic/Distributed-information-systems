package se.magnus.microservices.core.team;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import se.magnus.microservices.core.team.persistence.TeamEntity;
import se.magnus.microservices.core.team.persistence.TeamRepository;

import static org.junit.Assert.*;
import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED;

@RunWith(SpringRunner.class)
@DataJpaTest(properties = {"spring.cloud.config.enabled=false"})
@Transactional(propagation = NOT_SUPPORTED)
public class PersistenceTests {
    @Autowired
    private TeamRepository repository;

    private TeamEntity savedEntity;

    @Before
    public void setupDb() {
        repository.deleteAll().block();
        TeamEntity entity = new TeamEntity(1, 2, "a", "02.02.2021.", "c");
        savedEntity = repository.save(entity).block();
        assertEqualsTeam(entity, savedEntity);
    }


    @Test
    public void create() {
        TeamEntity newEntity = new TeamEntity(1, 3, "a", "02.02.2021.", "c");
        repository.save(newEntity).block();
        TeamEntity foundEntity = repository.findById(newEntity.getId()).block();
        assertEqualsTeam(newEntity, foundEntity);
        assertEquals(2, (long) repository.count().block());
    }

    @Test
    public void update() {
        savedEntity.setName("a2");
        repository.save(savedEntity).block();
        TeamEntity foundEntity = repository.findById(savedEntity.getId()).block();
        assertEquals(1, (long) foundEntity.getVersion());
        assertEquals("a2", foundEntity.getName());
    }

    @Test
    public void delete() {
        repository.delete(savedEntity).block();
        assertFalse(repository.existsById(savedEntity.getId()).block());
    }

    @Test
    public void getByTeamId() {
        TeamEntity entity = repository.findByTeamId(savedEntity.getLeagueId()).block();
        assertEqualsTeam(savedEntity, entity);
    }

    @Test(expected = DuplicateKeyException.class)
    public void duplicateError() {
        TeamEntity entity = new TeamEntity(1, 2, "a", "02.02.2021.", "c");
        repository.save(entity).block();
    }

    @Test
    public void optimisticLockError() {
        // Store the saved entity in two separate entity objects
        TeamEntity entity1 = repository.findById(savedEntity.getId()).block();
        TeamEntity entity2 = repository.findById(savedEntity.getId()).block();

        // Update the entity using the first entity object
        entity1.setName("a1");
        repository.save(entity1).block();

        //  Update the entity using the second entity object.
        // This should fail since the second entity now holds a old version number, i.e. a Optimistic Lock Error
        try {
            entity2.setName("a2");
            repository.save(entity2).block();
            fail("Expected an OptimisticLockingFailureException");
        } catch (OptimisticLockingFailureException ignored) {
        }

        // Get the updated entity from the database and verify its new sate
        TeamEntity updatedEntity = repository.findById(savedEntity.getId()).block();
        assertEquals(1, (int) updatedEntity.getVersion());
        assertEquals("a1", updatedEntity.getName());
    }

    private void assertEqualsTeam(TeamEntity expectedEntity, TeamEntity actualEntity) {
        assertEquals(expectedEntity.getId(), actualEntity.getId());
        assertEquals(expectedEntity.getVersion(), actualEntity.getVersion());
        assertEquals(expectedEntity.getTeamId(), actualEntity.getTeamId());
        assertEquals(expectedEntity.getLeagueId(), actualEntity.getLeagueId());
        assertEquals(expectedEntity.getName(), actualEntity.getName());
        assertEquals(expectedEntity.getCity(), actualEntity.getCity());
        assertEquals(expectedEntity.getFounded(), actualEntity.getFounded());
    }
}