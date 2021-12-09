package ru.region_stat.domain.dto.config;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConfigUpdateDto {

    private String code;

    private String name;

    private String value;
}