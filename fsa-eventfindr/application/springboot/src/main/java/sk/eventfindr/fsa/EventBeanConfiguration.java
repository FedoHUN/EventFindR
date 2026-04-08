package sk.eventfindr.fsa;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sk.eventfindr.fsa.domain.EventAttendanceRepository;
import sk.eventfindr.fsa.domain.EventRepository;
import sk.eventfindr.fsa.domain.UserRepository;
import sk.eventfindr.fsa.domain.service.EventFacade;
import sk.eventfindr.fsa.domain.service.EventService;

@Configuration
public class EventBeanConfiguration {

    @Bean
    public EventFacade eventFacade(EventRepository eventRepository,
                                   UserRepository userRepository,
                                   EventAttendanceRepository eventAttendanceRepository) {
        return new EventService(eventRepository, userRepository, eventAttendanceRepository);
    }
}
