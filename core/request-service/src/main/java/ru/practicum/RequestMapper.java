package ru.practicum;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.ParticipationRequestDto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RequestMapper {

    @Mapping(target = "event", source = "eventId")
    @Mapping(target = "created", source = "created")
    ParticipationRequestDto toDto(Request request);

    List<ParticipationRequestDto> toDtoList(List<Request> requests);
}
