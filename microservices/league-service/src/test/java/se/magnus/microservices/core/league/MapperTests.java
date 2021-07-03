package se.magnus.microservices.core.league;

import org.junit.Test;
import org.mapstruct.factory.Mappers;
import se.magnus.api.core.league.League;
import se.magnus.microservices.core.league.persistence.LeagueEntity;
import se.magnus.microservices.core.league.services.LeagueMapper;

import static org.junit.Assert.*;

public class MapperTests {
    private LeagueMapper mapper = Mappers.getMapper(LeagueMapper.class);

    @Test
    public void mapperTests() {
        assertNotNull(mapper);
        League api = new League(1, "a", "s", "adr");
        LeagueEntity entity = mapper.apiToEntity(api);
        assertEquals(api.getLeagueId(), entity.getLeagueId());
        assertEquals(api.getName(), entity.getName());
        assertEquals(api.getLabel(), entity.getLabel());
        League api2 = mapper.entityToApi(entity);
        assertEquals(api.getLeagueId(), api2.getLeagueId());
        assertEquals(api.getName(), api2.getName());
        assertEquals(api.getLabel(), api2.getLabel());
        assertNull(api2.getServiceAddress());
    }
}