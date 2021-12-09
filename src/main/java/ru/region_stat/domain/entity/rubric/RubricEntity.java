package ru.region_stat.domain.entity.rubric;

import lombok.*;
import ru.region_stat.domain.entity.BaseEntity;
import javax.persistence.*;

@Entity
@Table(name = "rs_pub_rubrics")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RubricEntity extends BaseEntity {

    @Column(name = "name")
    private String name;

    @Column(name = "archive_name")
    private String archiveName;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private RubricEntity parent;

    private Boolean isArchive;
}