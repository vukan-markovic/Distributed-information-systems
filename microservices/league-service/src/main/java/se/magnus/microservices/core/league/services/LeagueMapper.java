package se.magnus.microservices.core.league.services;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import se.magnus.api.core.league.League;
import se.magnus.microservices.core.league.persistence.LeagueEntity;

@Mapper(componentModel = "spring")
public interface LeagueMapper {
    @Mappings({
            @Mapping(target = "serviceAddress", ignore = true)
    })
    League entityToApi(LeagueEntity entity);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "version", ignore = true)
    })
    LeagueEntity apiToEntity(League api);
}