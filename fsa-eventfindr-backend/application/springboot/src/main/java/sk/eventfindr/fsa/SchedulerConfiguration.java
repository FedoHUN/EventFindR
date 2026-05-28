package sk.eventfindr.fsa;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import sk.eventfindr.fsa.domain.service.NotificationFacade;

@Configuration
@EnableScheduling
public class SchedulerConfiguration {

    private final NotificationFacade notificationFacade;

    public SchedulerConfiguration(NotificationFacade notificationFacade) {
        this.notificationFacade = notificationFacade;
    }

    @Scheduled(cron = "0 0 18 * * *")
    public void sendEventReminders() {
        notificationFacade.createEventReminders();
    }
}
