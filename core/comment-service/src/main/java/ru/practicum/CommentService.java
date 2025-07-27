package ru.practicum;



import ru.practicum.dto.CommentCreateDto;
import ru.practicum.dto.CommentDto;
import ru.practicum.dto.CommentUpdateDto;

import java.util.List;

public interface CommentService {
    CommentDto createComment(CommentCreateDto comment, Long userId, Long itemId);

    List<CommentDto> findCommentsByEventId(Long eventId, Integer from, Integer size);

    CommentDto updateCommentByAdmin(CommentUpdateDto updateDto, Long commentId);

    CommentDto getComment(Long commentId,Long eventId);

    void deleteCommentByAdmin(Long commentId);

    CommentDto updateCommentByUser(Long commentId, Long userId, Long eventId,CommentUpdateDto updateDto);

    void deleteCommentByUser(Long commentId, Long userId,Long eventId);
}
