package ru.region_stat.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.region_stat.domain.entity.rubric.RubricEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RubricRepository extends JpaRepository<RubricEntity, UUID> {
    Boolean existsByName(String name);

    Optional<RubricEntity> findByName(String rubricName);

    List<RubricEntity> findByIdIsIn(List<UUID> rubricEntityUUIDList);

    @Query(value = "SELECT * FROM rs_pub_rubrics WHERE id IN (SELECT DISTINCT rubric_id FROM rs_rubric_publication_link WHERE publication_id IN (SELECT DISTINCT id FROM rs_publications WHERE is_archive IS true))", nativeQuery = true)
    List<RubricEntity> getContainsArchivePublicationsRubrics();

    @Query(value = "SELECT * FROM rs_pub_rubrics WHERE id IN (SELECT DISTINCT rubric_id FROM rs_rubric_publication_link WHERE publication_id IN (SELECT DISTINCT id FROM rs_publications WHERE is_archive IS false))", nativeQuery = true)
    List<RubricEntity> getContainsPublicationsRubrics();
}