package ru.region_stat.domain.entity.publicationFile;

import lombok.*;
import ru.region_stat.domain.entity.BaseEntity;
import ru.region_stat.domain.entity.statisticalPublication.StatisticalPublicationEntity;
import javax.persistence.*;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import static ru.region_stat.domain.entity.publicationFile.PublicationFileEntity.TABLE;

@Entity
@Table(name = TABLE)
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PublicationFileEntity extends BaseEntity {

    public static final String TABLE = "rs_pub_files";
    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_extension")
    private String fileExtension;

    @Column(name = "size")
    private Long fileSize;

    @Column(name = "is_archive")
    private Boolean isArchive;

    @ManyToOne
    @JoinColumn(name = "publication_id")
    private StatisticalPublicationEntity statisticalPublication;

    @Min(1)
    @Max(31)
    @Column(name = "day")
    private Integer day;

    @Min(1)
    @Max(12)
    @Column(name = "month")
    private Integer month;

    @Digits(integer = 4, fraction = 0)
    @Column(name = "year")
    private Integer year;
}