package ru.region_stat.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.region_stat.domain.entity.config.ConfigEntity;
import java.util.UUID;

@Repository
public interface ConfigRepository extends JpaRepository<ConfigEntity, UUID> {
}