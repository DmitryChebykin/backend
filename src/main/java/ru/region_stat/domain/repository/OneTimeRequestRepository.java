package ru.region_stat.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.region_stat.domain.entity.oneTimeRequest.OneTimeRequestEntity;

import java.util.*;

@Repository
public interface OneTimeRequestRepository extends JpaRepository<OneTimeRequestEntity, UUID> {
    @Query(value = "select distinct q from OneTimeRequestEntity q left join fetch q.departmentEntity d left join fetch q.oneTimeRequestStatusEntity s left join fetch q.oneTimeRequestFileEntities ")
    List<OneTimeRequestEntity> getAllDetailedByQuery();

    Optional<OneTimeRequestEntity> findTopByOrderByPetrostatNumberDesc();

    Optional<OneTimeRequestEntity> findTopByOrderByInternalNumberDesc();

    Optional<OneTimeRequestEntity> findByPetrostatNumber(String number);

    @Modifying
    @Query(value = "update rs_one_time_requests set (publication_status_id, modified_by_user,last_modified_time) = (:statusId, :userId, :lastModifiedTime) where rs_one_time_requests.id in (:requestIds)  RETURNING CAST (rs_one_time_requests.id as VARCHAR),  CAST (rs_one_time_requests.publication_status_id AS VARCHAR)", nativeQuery = true)
    List<Map<UUID, UUID>> updateStatusByListIds(List<UUID> requestIds, UUID statusId, UUID userId, Date lastModifiedTime);
}