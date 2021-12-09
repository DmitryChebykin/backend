package ru.region_stat.domain.dto.calendarEvent;

import lombok.*;
import ru.region_stat.domain.dto.BaseResultDto;
import ru.region_stat.domain.dto.publicationType.PublicationTypeResultDto;
import java.util.Date;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CalendarEventResultDto extends BaseResultDto {
    private Integer day;
    private Integer month;
    private Integer year;
    private List<PublicationTypeResultDto> publicationTypeEntity;
    private Date localDate;

}