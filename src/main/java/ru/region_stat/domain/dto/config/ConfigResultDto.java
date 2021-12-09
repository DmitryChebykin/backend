package ru.region_stat.domain.dto.config;

import lombok.*;
import ru.region_stat.domain.dto.BaseResultDto;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConfigResultDto extends BaseResultDto {

    private String code;

    private String name;

    private String value;
}