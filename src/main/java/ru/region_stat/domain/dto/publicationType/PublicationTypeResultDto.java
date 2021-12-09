package ru.region_stat.domain.dto.publicationType;

import lombok.*;
import ru.region_stat.domain.dto.BaseResultDto;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PublicationTypeResultDto extends BaseResultDto {

    private String name;

    private String code;

    private String period;

    private String submissionTime;

    private String subscription;

    private List<String> rubricEntityId;

    private String formatEntityId;

    private List<CalendarEventDto> calendarEventDtoList;
}