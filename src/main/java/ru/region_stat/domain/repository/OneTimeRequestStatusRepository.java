package ru.region_stat.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.region_stat.domain.entity.oneTimeRequest.OneTimeRequestStatusEntity;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OneTimeRequestStatusRepository extends JpaRepository<OneTimeRequestStatusEntity, UUID> {
    Optional<OneTimeRequestStatusEntity> findByName(String name);

    Boolean existsByName(String name);
}