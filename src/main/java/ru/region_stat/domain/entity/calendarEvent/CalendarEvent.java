package ru.region_stat.domain.entity.calendarEvent;

import lombok.*;
import ru.region_stat.domain.entity.BaseEntity;
import ru.region_stat.domain.entity.publicationType.PublicationTypeEntity;
import javax.persistence.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "rs_event",
        uniqueConstraints = {@UniqueConstraint(name = "uniqueDate", columnNames = {"day", "month", "year"})})
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CalendarEvent extends BaseEntity {
    @Column(name = "day")
    private Integer day;

    @Column(name = "month")
    private Integer month;

    @Column(name = "year")
    private Integer year;

    @ManyToMany
    @JoinTable(
            name = "rs_event_pub_type_link",
            joinColumns = @JoinColumn(name = "event_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "pub_type_id", referencedColumnName = "id"),
            uniqueConstraints = {@UniqueConstraint(columnNames = {"event_id", "pub_type_id"})}
    )
    private List<PublicationTypeEntity> publicationTypeEntityList;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date")
    private Date localDate;

    @PrePersist
    @PreUpdate
    public void prePersist() {

        try {
            LocalDate date = LocalDate.of(this.year, this.month, this.day);
            this.localDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
        } catch (
                Exception e) {
            this.localDate = null;
        }
    }
}