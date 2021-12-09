package ru.region_stat.domain.dto.municipalFilesReference;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IndicatorResultDto {
    int moQuantity;
    private String cutOff;
    private String name;
}