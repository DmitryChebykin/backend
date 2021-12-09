package ru.region_stat.domain.entity.department;

import lombok.*;
import ru.region_stat.domain.entity.BaseEntity;
import ru.region_stat.domain.entity.oneTimeRequest.OneTimeRequestEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "rs_departments")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DepartmentEntity extends BaseEntity {

    @Column(name = "name", unique = true)
    private String name;

    @OneToMany(mappedBy = "departmentEntity")
    private List<OneTimeRequestEntity> timeRequestEntities;
}