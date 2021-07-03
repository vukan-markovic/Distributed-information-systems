package se.magnus.microservices.core.nationalTeam;

import org.junit.Test;
import org.mapstruct.factory.Mappers;
import se.magnus.api.core.nationalTeam.NationalTeam;
import se.magnus.microservices.core.nationalTeam.persistence.NationalTeamEntity;
import se.magnus.microservices.core.nationalTeam.services.NationalTeamMapper;

import static org.junit.Assert.*;

public class MapperTests {
    private NationalTeamMapper mapper = Mappers.getMapper(NationalTeamMapper.class);

    @Test
    public void mapperTests() {
        assertNotNull(mapper);
        NationalTeam api = new NationalTeam(1, "a", "s", "adr");
        NationalTeamEntity entity = mapper.apiToEntity(api);
        assertEquals(api.getNationalTeamId(), entity.getNationalTeamId());
        assertEquals(api.getName(), entity.getName());
        assertEquals(api.getTeamSelector(), entity.getTeamSelector());
        NationalTeam api2 = mapper.entityToApi(entity);
        assertEquals(api.getNationalTeamId(), api2.getNationalTeamId());
        assertEquals(api.getName(), api2.getName());
        assertEquals(api.getTeamSelector(), api2.getTeamSelector());
        assertNull(api2.getServiceAddress());
    }
}