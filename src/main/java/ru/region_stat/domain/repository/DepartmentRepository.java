package ru.region_stat.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.region_stat.domain.entity.department.DepartmentEntity;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DepartmentRepository extends JpaRepository<DepartmentEntity, UUID> {
    Optional<DepartmentEntity> findByName(String name);

    Boolean existsByName(String name);
}