package se.magnus.microservices.core.team;

import org.junit.Test;
import org.mapstruct.factory.Mappers;
import se.magnus.api.core.team.Team;
import se.magnus.microservices.core.team.persistence.TeamEntity;
import se.magnus.microservices.core.team.services.TeamMapper;

import static org.junit.Assert.*;

@SuppressWarnings("ALL")
public class MapperTests {
    private TeamMapper mapper = Mappers.getMapper(TeamMapper.class);

    @Test
    public void mapperTests() {
        assertNotNull(mapper);
        Team api = new Team(1, "a", "02-02-2021", "C", "adr");
        TeamEntity entity = mapper.apiToEntity(api);
        assertEquals(api.getTeamId(), entity.getTeamId());
        assertEquals(api.getName(), entity.getName());
        assertEquals(api.getFounded(), entity.getFounded());
        assertEquals(api.getCity(), entity.getCity());
        Team api2 = mapper.entityToApi(entity);
        assertEquals(api.getTeamId(), api2.getTeamId());
        assertEquals(api.getName(), api2.getName());
        assertEquals(api.getFounded(), api2.getFounded());
        assertEquals(api.getCity(), api2.getCity());
        assertNull(api2.getServiceAddress());
    }
}