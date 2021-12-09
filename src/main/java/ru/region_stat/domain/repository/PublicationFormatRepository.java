package ru.region_stat.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.region_stat.domain.entity.publicationFormat.PublicationFormatEntity;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PublicationFormatRepository extends JpaRepository<PublicationFormatEntity, UUID> {
    Boolean existsByName(String name);

    Optional<PublicationFormatEntity> findByName(String name);
}