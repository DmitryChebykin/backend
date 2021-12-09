package ru.region_stat.domain.entity.municipalFiles;

import lombok.*;
import ru.region_stat.domain.entity.BaseEntity;
import javax.persistence.*;

@Table(name = "rs_indicator")
@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IndicatorEntity extends BaseEntity {
    int moQuantity;
    private String cutOff;
    private String name;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "file_id")
    private MunicipalFilesReferenceEntity municipalFilesReferenceEntity;
}