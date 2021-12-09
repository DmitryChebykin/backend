package ru.region_stat.domain.dto.format;

import lombok.*;
import ru.region_stat.domain.dto.BaseResultDto;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PublicationFormatResultDto extends BaseResultDto {

    private String name;
}