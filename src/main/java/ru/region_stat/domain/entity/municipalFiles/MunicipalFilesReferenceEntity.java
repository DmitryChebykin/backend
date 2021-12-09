package ru.region_stat.domain.entity.municipalFiles;

import lombok.*;
import ru.region_stat.domain.entity.BaseEntity;
import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "rs_municipal_files_reference")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MunicipalFilesReferenceEntity extends BaseEntity {
    private String representationTerm;
    @Column(name = "file_name_pattern")
    private String fileNamePattern;
    private String periodType;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy =  "municipalFilesReferenceEntity" )
    private List<IndicatorEntity> indicatorEntityList;
}