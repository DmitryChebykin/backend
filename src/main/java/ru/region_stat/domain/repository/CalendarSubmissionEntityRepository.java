package ru.region_stat.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.region_stat.domain.entity.calendarEvent.CalendarSubmissionEntity;
import java.util.UUID;

public interface CalendarSubmissionEntityRepository extends JpaRepository<CalendarSubmissionEntity, UUID> {
}