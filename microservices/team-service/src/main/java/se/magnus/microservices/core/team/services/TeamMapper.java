package se.magnus.microservices.core.team.services;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import se.magnus.api.core.team.Team;
import se.magnus.microservices.core.team.persistence.TeamEntity;

@Mapper(componentModel = "spring")
public interface TeamMapper {
    @Mappings({
            @Mapping(target = "serviceAddress", ignore = true)
    })
    Team entityToApi(TeamEntity entity);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "version", ignore = true)
    })
    TeamEntity apiToEntity(Team api);
}