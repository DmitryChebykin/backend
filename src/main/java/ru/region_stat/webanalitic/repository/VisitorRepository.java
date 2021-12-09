package ru.region_stat.webanalitic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ru.region_stat.webanalitic.VisitorEntity;
import java.util.UUID;

@Repository
public interface VisitorRepository extends JpaRepository<VisitorEntity, UUID>, JpaSpecificationExecutor<VisitorEntity> {
}