package ru.region_stat.domain.dto.municipalFilesReference;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MunicipalFilesReferenceCreateDto {
    private String indicatorName;
    private String cutOff;
    private String representationTerm;
    private String fileName;
    private Integer moQuantity;
    private String periodType;
}