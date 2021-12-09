package ru.region_stat.domain.entity.statisticalPublication;

import lombok.*;
import ru.region_stat.domain.entity.BaseEntity;
import ru.region_stat.domain.entity.publicationFile.PublicationFileEntity;
import ru.region_stat.domain.entity.publicationFormat.PublicationFormatEntity;
import ru.region_stat.domain.entity.publicationType.PublicationTypeEntity;
import ru.region_stat.domain.entity.rubric.RubricEntity;
import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "rs_publications")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StatisticalPublicationEntity extends BaseEntity {

    @Column(name = "name")
    private String name;

    @Column(name = "complex_name")
    private String complexName;

    @Column(name = "is_archive")
    private Boolean isArchive;

    @ManyToOne
    @JoinColumn(name = "type_id")
    private PublicationTypeEntity publicationTypeEntity;

    @ManyToOne
    @JoinColumn(name = "format_id")
    private PublicationFormatEntity publicationFormatEntity;

    @OneToMany(mappedBy = "statisticalPublication")
    private List<PublicationFileEntity> publicationFileEntities;

    @ManyToMany
    @JoinTable(
            name = "rs_rubric_publication_link",
            joinColumns = @JoinColumn(name = "publication_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "rubric_id", referencedColumnName = "id"),
            uniqueConstraints = {@UniqueConstraint(columnNames = {"publication_id", "rubric_id"})}
    )
    private List<RubricEntity> rubricEntities;

    @Column(name = "archive_rubric_name")
    private String archiveRubricName;
}