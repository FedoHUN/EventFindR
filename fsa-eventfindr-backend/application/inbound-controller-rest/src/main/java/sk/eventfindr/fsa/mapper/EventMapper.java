package sk.eventfindr.fsa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import sk.eventfindr.fsa.domain.Event;
import sk.eventfindr.fsa.domain.EventArtist;
import sk.eventfindr.fsa.domain.EventComment;
import sk.eventfindr.fsa.domain.EventStatus;
import sk.eventfindr.fsa.domain.Notification;
import sk.eventfindr.fsa.domain.NotificationType;
import sk.eventfindr.fsa.rest.dto.CreateEventRequestDto;
import sk.eventfindr.fsa.rest.dto.EventArtistDto;
import sk.eventfindr.fsa.rest.dto.EventArtistRequestDto;
import sk.eventfindr.fsa.rest.dto.EventCommentDto;
import sk.eventfindr.fsa.rest.dto.EventDto;
import sk.eventfindr.fsa.rest.dto.NotificationDto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface EventMapper {

    EventDto toDto(Event event);

    @AfterMapping
    default void applyPresentationFields(Event event, @MappingTarget EventDto dto) {
        if ((dto.getImageUrl() == null || dto.getImageUrl().isBlank())
                && event.getCoverImageMediaId() != null) {
            dto.setImageUrl("/events/" + event.getId() + "/media/" + event.getCoverImageMediaId() + "/file");
        }
    }

    List<EventDto> toDtoList(Collection<Event> events);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "organizer", ignore = true)
    @Mapping(target = "canceled", ignore = true)
    @Mapping(target = "featured", ignore = true)
    @Mapping(target = "attendingCount", ignore = true)
    @Mapping(target = "watchingCount", ignore = true)
    @Mapping(target = "commentCount", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "ratingCount", ignore = true)
    @Mapping(target = "coverImageMediaId", ignore = true)
    Event toEntity(CreateEventRequestDto request);

    EventArtistDto toArtistDto(EventArtist artist);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eventId", ignore = true)
    @Mapping(target = "sortOrder", ignore = true)
    EventArtist toArtistEntity(EventArtistRequestDto request);

    EventCommentDto toCommentDto(EventComment comment);

    List<EventCommentDto> toCommentDtoList(Collection<EventComment> comments);

    NotificationDto toNotificationDto(Notification notification);

    List<NotificationDto> toNotificationDtoList(Collection<Notification> notifications);

    default EventDto.StatusEnum mapEventStatus(EventStatus status) {
        return status == null ? null : EventDto.StatusEnum.fromValue(status.name());
    }

    default EventStatus mapEventStatusDto(CreateEventRequestDto.StatusEnum status) {
        return status == null ? null : EventStatus.valueOf(status.name());
    }

    default NotificationDto.TypeEnum mapNotificationType(NotificationType type) {
        return type == null ? null : NotificationDto.TypeEnum.fromValue(type.name());
    }

    default OffsetDateTime map(Date value) {
        return value == null ? null : value.toInstant().atOffset(ZoneOffset.UTC);
    }

    default Date mapToDate(OffsetDateTime value) {
        return value == null ? null : Date.from(value.toInstant());
    }

    default Double mapBigDecimal(BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }

    default BigDecimal mapDouble(Double value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }
}
