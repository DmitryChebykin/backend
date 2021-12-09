package ru.region_stat.domain.dto.oneTimeRequestStatus;

import lombok.*;
import ru.region_stat.domain.dto.BaseResultDto;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class OneTimeRequestStatusResultDto extends BaseResultDto {
    private String name;
}