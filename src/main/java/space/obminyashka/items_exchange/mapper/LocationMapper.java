package space.obminyashka.items_exchange.mapper;


import org.mapstruct.Mapper;
import space.obminyashka.items_exchange.dto.LocationDto;
import space.obminyashka.items_exchange.model.Location;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LocationMapper {
    LocationDto toDto(Location model);
    Location toModel(LocationDto dto);

    List<LocationDto> toDtoList(List<Location> modelList);
    List<Location> toModelList(List<LocationDto> dtoList);
}
