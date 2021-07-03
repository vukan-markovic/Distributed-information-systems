package se.magnus.microservices.core.player.services;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import se.magnus.api.core.player.Player;
import se.magnus.microservices.core.player.persistence.PlayerEntity;

@Mapper(componentModel = "spring")
public interface PlayerMapper {

    @Mappings({
            @Mapping(target = "serviceAddress", ignore = true)
    })
    Player entityToApi(PlayerEntity entity);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "version", ignore = true)
    })
    PlayerEntity apiToEntity(Player api);
}