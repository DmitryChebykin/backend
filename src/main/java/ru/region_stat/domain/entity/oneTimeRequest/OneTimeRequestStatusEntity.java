package ru.region_stat.domain.entity.oneTimeRequest;

import lombok.*;
import ru.region_stat.domain.entity.BaseEntity;
import javax.persistence.Entity;
import javax.persistence.Table;

@Table(name = "rs_request_statuses")
@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OneTimeRequestStatusEntity extends BaseEntity {
    private String name;
}