package ru.region_stat.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.region_stat.domain.entity.calendarEvent.CalendarEvent;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CalendarEventRepository extends JpaRepository<CalendarEvent, UUID> {
    Optional<List<CalendarEvent>> findAllByLocalDateIsBetween(Date start, Date end);

    Optional<CalendarEvent> findByDayAndMonthAndYear(Integer day, Integer month, Integer year);

    @Query(value = "select * from rs_event where rs_event.id in (select event_id from rs_event_pub_type_link where rs_event_pub_type_link.pub_type_id = :publicationId)", nativeQuery = true)
    Optional<List<CalendarEvent>> getEventsByPublicationType(UUID publicationId);

    @Query(value = " select case when exists (select * from rs_event_pub_type_link where event_id = :calendarId and pub_type_id = :publicationeTypeId) then 'true' else 'false' end", nativeQuery = true)
    Boolean publicationEventExistByCalendarEventIdAndPublicationId(UUID calendarId, UUID publicationeTypeId);

    @Modifying
    @Query(value = " delete from rs_event_pub_type_link where pub_type_id = :id and event_id not in (:actualCalendarEventIdList)", nativeQuery = true)
    void removeUnnesessaryCalendarEvents(List<UUID> actualCalendarEventIdList, UUID id);
}