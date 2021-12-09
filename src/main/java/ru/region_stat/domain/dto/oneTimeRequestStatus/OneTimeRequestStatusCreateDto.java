package ru.region_stat.domain.dto.oneTimeRequestStatus;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class OneTimeRequestStatusCreateDto {
    private String name;
}