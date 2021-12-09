package ru.region_stat.domain.dto.publicationType;

import lombok.*;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PublicationTypeCreateNewDto {
    private String name;
    private String code;
    private String period;
    private String submissionTime;
    private String subscription;
    private List<String> rubricEntityId;
    private String formatEntityId;
    private List<CalendarEventDto> calendarEventDtoList;
}