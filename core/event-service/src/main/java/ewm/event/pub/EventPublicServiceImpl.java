package ewm.event.pub;

import ewm.event.Event;
import ewm.event.EventRepository;
import ewm.event.mapper.EventMapper;
import ewm.src.main.java.ru.practicum.AnalyzerClient;
import ewm.src.main.java.ru.practicum.CollectorClient;
import ru.practicum.dto.*;
import ru.practicum.exception.NotFoundException;
import ewm.utils.CheckEventService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.exception.ValidationException;
import ru.practicum.feign.RequestClient;
import ru.practicum.feign.UserClient;
import ru.yandex.practicum.grpc.telemetry.user.RecommendedEventProto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EventPublicServiceImpl implements EventPublicService {

    private EventRepository eventRepository;
    private EventMapper eventMapper;
    private CheckEventService checkEventService;
    private UserClient userClient;
    private AnalyzerClient analyzerClient;
    private RequestClient requestClient;


    private String app;
    private CollectorClient collectorClient;

    public EventPublicServiceImpl(EventRepository eventRepository,
                                  EventMapper eventMapper,
                                  CheckEventService checkEventService,
                                  UserClient userClient,
                                  @Value("${my.app}") String app,
                                  CollectorClient collectorClient,
                                  AnalyzerClient analyzerClient,
                                  RequestClient requestClient) {
        this.eventRepository = eventRepository;
        this.eventMapper = eventMapper;
        this.checkEventService = checkEventService;
        this.app = app;
        this.userClient = userClient;
        this.collectorClient = collectorClient;
        this.analyzerClient = analyzerClient;
        this.requestClient = requestClient;
    }

    static LocalDateTime minTime = LocalDateTime.of(1970, 1, 1, 0, 0);
    static LocalDateTime maxTime = LocalDateTime.of(3000, 1, 1, 0, 0);
    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    @Override
    public List<EventShortDto> getEvents(String text, List<Long> categories, Boolean paid,
                                         String rangeStart, String rangeEnd, Boolean onlyAvailable,
                                         String sort, Integer from, Integer size, HttpServletRequest request) {
        Pageable pageable = PageRequest.of(from / size, size);

        LocalDateTime start = rangeStart != null ? LocalDateTime.parse(rangeStart, formatter) : minTime;
        LocalDateTime end = rangeEnd != null ? LocalDateTime.parse(rangeEnd, formatter) : maxTime;
        text = text != null ? text : "";
        Page<Event> events = eventRepository.findEvents(text, paid, start, end, categories, onlyAvailable,
                State.PUBLISHED, pageable);
        List<Long> userIds = events.stream().map(Event::getInitiatorId).toList();
        if (userIds.isEmpty()) {
            throw new ValidationException("Нет подходящих событий");
        }
        List<UserShortDto> usersDto = userClient.getUsers(userIds, 0, userIds.size()).stream()
                .map(userDto -> UserShortDto.builder().id(userDto.getId()).name(userDto.getName()).build()).toList();
        List<EventShortDto> dtos = events.map(event -> eventMapper.toShortDto(event, usersDto.stream()
                .filter(userShortDto -> userShortDto.getId().equals(event.getInitiatorId())).findAny()
                .orElseThrow(() -> new NotFoundException("Пользователя с таким id нет")))).toList();

        if (sort != null) {
            dtos = events.stream()
                    .sorted((event1, event2) -> {
                        if (sort.equals("EVENT_DATE")) {
                            if (event1.getEventDate().isBefore(event2.getEventDate()))
                                return -1;
                            else
                                return 1;
                        } else if (sort.equals("RATING")) {
                            return (int) (event1.getRating() - event2.getRating());
                        }
                        return 1;
                    })
                    .map(event -> eventMapper.toShortDto(event, usersDto.stream()
                            .filter(userShortDto -> userShortDto.getId().equals(event.getInitiatorId())).findAny()
                            .orElseThrow(() -> new NotFoundException("Пользователя с таким id нет"))))
                    .toList();
        }
        // Получаем список URI для всех событий

        // Устанавливаем количество просмотров для каждого события
        List<RecommendedEventProto> ratings = analyzerClient.getInteractionsCount(dtos.stream()
                .map(EventShortDto::getId).toList()).toList();
        Map<Long, Double> ratingMap = ratings.stream()
                .collect(Collectors.toMap(RecommendedEventProto::getEventId, RecommendedEventProto::getScore));
        dtos.forEach(dto -> dto.setRating(ratingMap.get(dto.getId())));

        if (dtos.isEmpty()) {
            throw new ValidationException("Нет подходящих событий");
        } else {
            return dtos;
        }
    }

    @Override
    public EventFullDto getEventById(Long userId, Long id, HttpServletRequest request) {
        Event event = checkEventService.checkPublishedEvent(id);
        UserDto userDto = userClient.getUser(event.getInitiatorId());
        EventFullDto eventFullDto = eventMapper.toFullDto(event, UserShortDto.builder().id(userDto.getId())
                .name(userDto.getName()).build());
        collectorClient.collectUserAction(userId, id, "ACTION_VIEW");
        List<RecommendedEventProto> ratings = analyzerClient.getInteractionsCount(List.of(id)).toList();
        eventFullDto.setRating(ratings.getFirst().getScore());
        return eventFullDto;
    }

    @Override
    public EventFullDto getEventByIdInternal(Long id) {
        Event event = eventRepository.findById(id).orElseThrow(() -> new NotFoundException("Ивента с таким id нет"));
        UserDto userDto = userClient.getUser(event.getInitiatorId());
        return eventMapper.toFullDto(event, UserShortDto.builder().id(userDto.getId())
                .name(userDto.getName()).build());
    }


    @Override
    public void changeEventFields(EventFullDto eventFullDto) {
        Event event = eventRepository.findById(eventFullDto.getId())
                .orElseThrow(() -> new NotFoundException("События с таким id нет"));
        if (!eventFullDto.getConfirmedRequests().equals(event.getConfirmedRequests())) {
            event.setConfirmedRequests(eventFullDto.getConfirmedRequests());
        }
        eventRepository.save(event);

    }

    @Override
    public List<EventShortDto> getRecommendations(Long userId, int maxResult) {
        List<Event> events = eventRepository.findByIdIn(analyzerClient.getRecommendationsForUser(userId, maxResult)
                .map(RecommendedEventProto::getEventId).toList());
        List<UserShortDto> users = userClient
                .getUsers(events.stream().map(Event::getInitiatorId).toList(), 0, events.size()).stream()
                .map(userDto -> UserShortDto.builder().id(userDto.getId()).name(userDto.getName()).build()).toList();
        return events.stream().map(e -> eventMapper.toShortDto(e, users.stream().filter(u -> u.getId()
                        .equals(e.getInitiatorId())).findAny()
                .orElseThrow(() -> new NotFoundException("Пользователя с таким id нет")))).toList();
    }

    @Override
    public void putLike(long userId, long eventId) {
        List<ParticipationRequestDto> requests = requestClient.findRequestsByUserId(userId);
        Optional<ParticipationRequestDto> request = requests.stream().filter(s -> s.getEvent().equals(eventId)).findAny();
        if (request.isPresent() && request.get().getStatus().equals(RequestStatus.CONFIRMED)) {
            collectorClient.collectUserAction(userId, eventId, "ACTION_LIKE");
        } else {
            throw new ValidationException("Пользователь не может ставить лайки на непосещенные события");
        }


    }
}
