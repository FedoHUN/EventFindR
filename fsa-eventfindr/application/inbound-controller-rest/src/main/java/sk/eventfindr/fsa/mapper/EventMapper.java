package sk.eventfindr.fsa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sk.eventfindr.fsa.domain.Event;
import sk.eventfindr.fsa.rest.dto.CreateEventRequestDto;
import sk.eventfindr.fsa.rest.dto.EventDto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface EventMapper {

    EventDto toDto(Event event);

    List<EventDto> toDtoList(Collection<Event> events);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "organizer", ignore = true)
    Event toEntity(CreateEventRequestDto request);

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
