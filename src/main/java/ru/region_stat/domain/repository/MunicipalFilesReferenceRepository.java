package ru.region_stat.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ru.region_stat.domain.entity.municipalFiles.MunicipalFilesReferenceEntity;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MunicipalFilesReferenceRepository extends JpaRepository<MunicipalFilesReferenceEntity, UUID>, JpaSpecificationExecutor<MunicipalFilesReferenceEntity> {
    Optional<MunicipalFilesReferenceEntity> findByFileNamePattern(String fileNamePattern);
}