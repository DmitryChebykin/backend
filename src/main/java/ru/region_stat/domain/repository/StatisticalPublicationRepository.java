package ru.region_stat.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.region_stat.domain.dto.statisticalPublication.StatisticalPublicationEntityIdInfo;
import ru.region_stat.domain.entity.rubric.RubricEntity;
import ru.region_stat.domain.entity.statisticalPublication.StatisticalPublicationEntity;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Repository
public interface StatisticalPublicationRepository extends JpaRepository<StatisticalPublicationEntity, UUID> {

    List<StatisticalPublicationEntity> findAllByRubricEntitiesIn(Collection<RubricEntity> rubricEntityList);

    @Modifying
    @Query(value = "update rs_publications set is_archive = not is_archive, last_modified_time = :date, modified_by_user = :userId where rs_publications.id in (:uuidList)  RETURNING *", nativeQuery = true)
    List<StatisticalPublicationEntity> invertIsArchive(@Param("uuidList") List<UUID> uuidList, UUID userId, Date date);

    @Query(value = "SELECT distinct CAST (id AS VARCHAR) FROM rs_publications", nativeQuery = true)
    List<StatisticalPublicationEntityIdInfo> getAllId();

    @Query(value = "SELECT distinct CAST (id AS VARCHAR) FROM rs_publications WHERE id NOT IN (:idList)", nativeQuery = true)
    List<StatisticalPublicationEntityIdInfo> getAllIdNotInList(@Param("idList") List<UUID> idList);

    @Query(value = "SELECT DISTINCT archive_rubric_name FROM rs_publications WHERE archive_rubric_name IS NOT NULL", nativeQuery = true)
    List<String> getArchiveRubricsNames();

    @Query(value = "SELECT p  FROM StatisticalPublicationEntity  p WHERE p.isArchive = true AND p.archiveRubricName LIKE %:tag%")
    List<StatisticalPublicationEntity> getPublicationsByArchivedRubricNames(@Param("tag") String tag);
}