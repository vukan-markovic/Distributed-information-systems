package se.magnus.microservices.core.player;

import org.junit.Test;
import org.mapstruct.factory.Mappers;
import se.magnus.api.core.player.Player;
import se.magnus.microservices.core.player.persistence.PlayerEntity;
import se.magnus.microservices.core.player.services.PlayerMapper;

import static org.junit.Assert.*;

@SuppressWarnings("ALL")
public class MapperTests {
    private PlayerMapper mapper = Mappers.getMapper(PlayerMapper.class);

    @Test
    public void mapperTests() {
        assertNotNull(mapper);
        Player api = new Player(1, "n", "b", "c", "d", 1, 1, 1, 1, "sa");
        PlayerEntity entity = mapper.apiToEntity(api);
        assertEquals(api.getPlayerId(), entity.getPlayerId());
        assertEquals(api.getName(), entity.getName());
        assertEquals(api.getSurname(), entity.getSurname());
        assertEquals(api.getRegistrationNumber(), entity.getRegistrationNumber());
        assertEquals(api.getDateOfBirth(), entity.getDateOfBirth());
        Player api2 = mapper.entityToApi(entity);
        assertEquals(api.getPlayerId(), api2.getPlayerId());
        assertEquals(api.getName(), api2.getName());
        assertEquals(api.getSurname(), api2.getSurname());
        assertEquals(api.getRegistrationNumber(), api2.getRegistrationNumber());
        assertEquals(api.getDateOfBirth(), api2.getDateOfBirth());
        assertNull(api2.getServiceAddress());
    }
}