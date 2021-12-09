package ru.region_stat.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.region_stat.domain.dto.oneTimeRequest.OneTimeRequestFileEntityFileName;
import ru.region_stat.domain.entity.oneTimeRequest.OneTimeRequestFileEntity;
import java.util.UUID;

@Repository
public interface OneTimeRequestFileRepository extends JpaRepository<OneTimeRequestFileEntity, UUID> {
    @Query(value = "SELECT file_name from rs_one_time_request_files WHERE id = :id", nativeQuery = true)
    OneTimeRequestFileEntityFileName getFileNameByFileId(UUID id);
}