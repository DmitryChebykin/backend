package ru.region_stat.domain.entity.calendarEvent;

import lombok.*;
import ru.region_stat.domain.entity.BaseEntity;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "rs_calendar_submission")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CalendarSubmissionEntity extends BaseEntity {

    private UUID eventId;

    private UUID publicationTypeId;
}