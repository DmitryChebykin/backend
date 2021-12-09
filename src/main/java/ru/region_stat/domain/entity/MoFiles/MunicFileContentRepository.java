package ru.region_stat.domain.entity.MoFiles;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface MunicFileContentRepository extends JpaRepository<MunicFileContentEntity, UUID> {
}