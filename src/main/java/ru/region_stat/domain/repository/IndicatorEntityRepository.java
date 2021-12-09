package ru.region_stat.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.region_stat.domain.entity.municipalFiles.IndicatorEntity;
import java.util.Optional;
import java.util.UUID;

public interface IndicatorEntityRepository extends JpaRepository<IndicatorEntity, UUID> {
    Optional<IndicatorEntity> findByName(String name);
}