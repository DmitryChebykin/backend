package ru.region_stat.domain.entity.config;

import lombok.*;
import ru.region_stat.domain.entity.BaseEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "rs_configs")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConfigEntity extends BaseEntity {

    @Column(name = "code", unique = true)
    private String code;

    @Column(name = "name", unique = true)
    private String name;

    @Column(name = "value", columnDefinition = "TEXT")
    private String value;
}