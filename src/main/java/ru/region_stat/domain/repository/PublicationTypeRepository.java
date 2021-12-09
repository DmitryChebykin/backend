package ru.region_stat.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.region_stat.domain.entity.publicationType.PublicationTypeEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PublicationTypeRepository extends JpaRepository<PublicationTypeEntity, UUID> {
    Boolean existsByName(String name);

    Optional<PublicationTypeEntity> findByName(String name);

    Boolean existsByCode(String fullCode);

    PublicationTypeEntity findByCodeIs(String code);

    Optional<List<PublicationTypeEntity>> findByIdIn(List<UUID> publicationTypeIdList);
}