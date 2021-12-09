package ru.region_stat.domain.dto.municipalFilesReference;

import lombok.*;
import ru.region_stat.domain.dto.BaseResultDto;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MunicipalFilesReferenceResultDto extends BaseResultDto {
    private String indicatorName;
    private String cutOff;
    private String representationTerm;
    private String fileName;
    private Integer moQuantity;
    private String periodType;
}