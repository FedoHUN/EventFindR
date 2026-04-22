package sk.eventfindr.fsa.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import sk.eventfindr.fsa.domain.User;
import sk.eventfindr.fsa.domain.UserRole;

import java.util.Collection;
import java.util.Optional;

interface UserSpringDataRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Collection<User> findByRola(UserRole rola);
}
