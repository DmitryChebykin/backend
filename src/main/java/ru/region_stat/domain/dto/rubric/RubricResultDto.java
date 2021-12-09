package ru.region_stat.domain.dto.rubric;

import lombok.*;
import ru.region_stat.domain.dto.BaseResultDto;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RubricResultDto extends BaseResultDto {

    private String name;

    private String archiveName;

    private String parentId;

    private Boolean isArchive;
}