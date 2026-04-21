package sk.eventfindr.fsa;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sk.eventfindr.fsa.domain.UserRepository;
import sk.eventfindr.fsa.domain.service.UserFacade;
import sk.eventfindr.fsa.domain.service.UserService;

@Configuration
public class UserBeanConfiguration {

    @Bean
    public UserFacade userFacade(UserRepository userRepository) {
        return new UserService(userRepository);
    }
}
