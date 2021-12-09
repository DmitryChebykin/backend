package ru.region_stat.domain.entity.MoFiles;

import lombok.*;
import ru.region_stat.domain.entity.BaseEntity;
import javax.persistence.*;

@Entity
@Table(name = "rs_munic_files")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MunicFileEntity extends BaseEntity {

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_extension")
    private String fileExtension;

    @Column(name = "size")
    private Long fileSize;

    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY, mappedBy = "municFileEntity")
    private MunicFileContentEntity municFileContentEntity;

    private String representationTerm;
    private String fileNamePattern;
    private String realFileName;
    private String periodType;
    private String year;
    private String path;
    private String createdDate;
    private String modifiedDate;
    private String indicatorDescription;
}