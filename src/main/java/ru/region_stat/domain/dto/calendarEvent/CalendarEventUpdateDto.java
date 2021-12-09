package ru.region_stat.domain.dto.calendarEvent;

import lombok.*;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CalendarEventUpdateDto {
    private Integer day;
    private Integer month;
    private Integer year;
    private List<UUID> publicationTypeIdList;
}