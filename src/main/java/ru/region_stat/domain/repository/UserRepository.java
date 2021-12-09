package ru.region_stat.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.region_stat.domain.entity.user.UserEntity;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Boolean existsByNtlmLogin(String login);

    UserEntity findByNtlmLoginIs(String login);

    Optional<UserEntity> findByEmailIs(String userEmail);

    UserEntity findByLogin(String login);

    Optional<UserEntity> findByNtlmLogin(String ntlmLogin);
}