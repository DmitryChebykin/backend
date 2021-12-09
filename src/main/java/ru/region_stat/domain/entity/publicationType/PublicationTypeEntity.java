package ru.region_stat.domain.entity.publicationType;

import lombok.*;
import ru.region_stat.domain.entity.BaseEntity;
import ru.region_stat.domain.entity.publicationFormat.PublicationFormatEntity;
import ru.region_stat.domain.entity.rubric.RubricEntity;
import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "rs_pub_types")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PublicationTypeEntity extends BaseEntity {
    @Column(name = "subscription_type")
    private Subscription subscription;

    @Column(name = "name")
    private String name;

    @Column(name = "code", unique = true)
    private String code;

    @Column(name = "period")
    private String period;

    @Column(name = "submission_time")
    private String submissionTime;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "format_id")
    private PublicationFormatEntity publicationFormatEntity;

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(
            name = "rs_rubric_type_link",
            joinColumns = @JoinColumn(name = "type_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "rubric_id", referencedColumnName = "id"),
            uniqueConstraints = {@UniqueConstraint(columnNames = {"type_id", "rubric_id"})}
    )
    private List<RubricEntity> rubricEntityList;
}