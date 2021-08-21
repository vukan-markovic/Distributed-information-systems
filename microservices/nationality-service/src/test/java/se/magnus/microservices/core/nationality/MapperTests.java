package se.magnus.microservices.core.nationality;

import org.junit.Test;
import org.mapstruct.factory.Mappers;
import se.magnus.api.core.nationality.Nationality;
import se.magnus.microservices.core.nationality.persistence.NationalityEntity;
import se.magnus.microservices.core.nationality.services.NationalityMapper;

import static org.junit.Assert.*;

@SuppressWarnings("ALL")
public class MapperTests {
    private NationalityMapper mapper = Mappers.getMapper(NationalityMapper.class);

    @Test
    public void mapperTests() {
        assertNotNull(mapper);
        Nationality api = new Nationality(1, "a", "s", "adr");
        NationalityEntity entity = mapper.apiToEntity(api);
        assertEquals(api.getNationalityId(), entity.getNationalityId());
        assertEquals(api.getName(), entity.getName());
        assertEquals(api.getAbbreviation(), entity.getAbbreviation());
        Nationality api2 = mapper.entityToApi(entity);
        assertEquals(api.getNationalityId(), api2.getNationalityId());
        assertEquals(api.getName(), api2.getName());
        assertEquals(api.getAbbreviation(), api2.getAbbreviation());
        assertNull(api2.getServiceAddress());
    }
}