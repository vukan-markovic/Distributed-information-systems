package se.magnus.microservices.core.nationality.services;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import se.magnus.api.core.nationality.Nationality;
import se.magnus.microservices.core.nationality.persistence.NationalityEntity;

@Mapper(componentModel = "spring")
public interface NationalityMapper {
    @Mappings({
            @Mapping(target = "serviceAddress", ignore = true)
    })
    Nationality entityToApi(NationalityEntity entity);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "version", ignore = true)
    })
    NationalityEntity apiToEntity(Nationality api);
}