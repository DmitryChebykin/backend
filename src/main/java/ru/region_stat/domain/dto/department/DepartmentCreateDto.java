package ru.region_stat.domain.dto.department;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DepartmentCreateDto {

    private String name;
}