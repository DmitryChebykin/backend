package ru.region_stat.domain.dto.publicationType;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CalendarEventDto {
    private Integer day;
    private Integer month;
    private Integer year;
}