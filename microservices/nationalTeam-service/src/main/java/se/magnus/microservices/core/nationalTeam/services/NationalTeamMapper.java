package se.magnus.microservices.core.nationalTeam.services;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import se.magnus.api.core.nationalTeam.NationalTeam;
import se.magnus.microservices.core.nationalTeam.persistence.NationalTeamEntity;

@Mapper(componentModel = "spring")
public interface NationalTeamMapper {
    @Mappings({
            @Mapping(target = "serviceAddress", ignore = true)
    })
    NationalTeam entityToApi(NationalTeamEntity entity);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "version", ignore = true)
    })
    NationalTeamEntity apiToEntity(NationalTeam api);
}