package ru.region_stat.domain.dto.municipalFilesReference;

import lombok.*;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MunicipalFilesReferencePreviewDto {
    private String id;
    private List<IndicatorResultDto> indicatorEntityList;
    private String representationTerm;
    private String fileNamePattern;
    private String realFileName;
    private String periodType;
    private boolean isFolder;
}