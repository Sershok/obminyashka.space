package space.obminyashka.items_exchange.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import space.obminyashka.items_exchange.model.User;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmailOrUsername(String username, String email);

    @Query("select u.updated from User u where u.username = :username")
    LocalDateTime selectLastUpdatedTimeFromUserByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsernameOrEmail(String username, String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByRefreshToken_Token(String token);

    @Transactional
    @Modifying
    @Query("update User u set u.avatarImage = null where u.username = :username")
    void cleanAvatarForUserByName(String username);

    @Transactional
    @Modifying
    @Query("update User u set u.oauth2Login = true where u.email = :email and u.oauth2Login is null")
    void setOAuth2LoginToUserByEmail(String email);
}
