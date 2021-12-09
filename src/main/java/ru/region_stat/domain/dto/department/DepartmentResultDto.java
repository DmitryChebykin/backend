package ru.region_stat.domain.dto.department;

import lombok.*;
import ru.region_stat.domain.dto.BaseResultDto;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DepartmentResultDto extends BaseResultDto {

    private String name;
}