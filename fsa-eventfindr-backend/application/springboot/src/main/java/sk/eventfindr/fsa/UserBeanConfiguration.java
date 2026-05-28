package sk.eventfindr.fsa;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;
import sk.eventfindr.fsa.domain.User;
import sk.eventfindr.fsa.domain.UserRepository;
import sk.eventfindr.fsa.domain.service.UserFacade;
import sk.eventfindr.fsa.domain.service.UserService;

import java.util.Collection;
import java.util.Optional;

@Configuration
public class UserBeanConfiguration {

    @Bean
    public UserFacade userFacade(UserRepository userRepository) {
        UserFacade userService = new UserService(userRepository, EventBeanConfiguration.logger(UserService.class));
        return new TransactionalUserFacade(userService);
    }

    @Transactional
    static class TransactionalUserFacade implements UserFacade {

        private final UserFacade delegate;

        TransactionalUserFacade(UserFacade delegate) {
            this.delegate = delegate;
        }

        @Override
        @Transactional(readOnly = true)
        public Optional<User> get(long id) {
            return delegate.get(id);
        }

        @Override
        @Transactional(readOnly = true)
        public Optional<User> get(String email) {
            return delegate.get(email);
        }

        @Override
        @Transactional(readOnly = true)
        public Collection<User> getOrganizers() {
            return delegate.getOrganizers();
        }

        @Override
        public void create(User user) {
            delegate.create(user);
        }

        @Override
        public void becomeOrganizer(String email, String organizationName) {
            delegate.becomeOrganizer(email, organizationName);
        }

        @Override
        public void becomeArtist(String email, String artistName) {
            delegate.becomeArtist(email, artistName);
        }

        @Override
        public void updateOrganizationName(String email, String organizationName) {
            delegate.updateOrganizationName(email, organizationName);
        }

        @Override
        public void updateOrganizationDescription(String email, String description) {
            delegate.updateOrganizationDescription(email, description);
        }

        @Override
        public void updateArtistDescription(String email, String description) {
            delegate.updateArtistDescription(email, description);
        }

        @Override
        @Transactional(readOnly = true)
        public Collection<User> searchArtists(String nameFragment) {
            return delegate.searchArtists(nameFragment);
        }
    }
}
