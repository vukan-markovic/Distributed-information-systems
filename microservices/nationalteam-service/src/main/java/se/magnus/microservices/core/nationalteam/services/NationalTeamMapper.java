package se.magnus.microservices.core.nationalteam.services;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import se.magnus.api.core.nationalteam.NationalTeam;
import se.magnus.microservices.core.nationalteam.persistence.NationalTeamEntity;

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