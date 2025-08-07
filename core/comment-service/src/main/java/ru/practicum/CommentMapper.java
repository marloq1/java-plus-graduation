package ru.practicum;


import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.Mapper;
import ru.practicum.dto.CommentCreateDto;
import ru.practicum.dto.CommentDto;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "id", ignore = true)
    Comment toEntity(CommentCreateDto commentCreateDto);

    @Mapping(target = "event", source = "eventId")
    @Mapping(target = "timestamp", source = "comment", qualifiedByName = "mapTimestamp")
    @Mapping(target = "updated", source = "comment", qualifiedByName = "mapUpdated")
    CommentDto toDto(Comment comment);

    @Named("mapTimestamp")
    default LocalDateTime mapTimestamp(Comment comment) {
        return comment.getUpdated();
    }

    @Named("mapUpdated")
    default Boolean mapUpdated(Comment comment) {
        return !comment.getUpdated().equals(comment.getCreated());
    }
}
