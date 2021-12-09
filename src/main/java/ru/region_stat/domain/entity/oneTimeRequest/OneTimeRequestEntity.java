package ru.region_stat.domain.entity.oneTimeRequest;

import lombok.*;
import ru.region_stat.domain.entity.BaseEntity;
import ru.region_stat.domain.entity.department.DepartmentEntity;
import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "rs_one_time_requests")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OneTimeRequestEntity extends BaseEntity {

    @Column(name = "theme", columnDefinition = "TEXT")
    private String theme;

    @Column(name = "identificator")
    private Integer identificator;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "petrostat_text", columnDefinition = "TEXT")
    private String petrostatText;

    @Column(name = "resolution", columnDefinition = "TEXT")
    private String resolution;

    @Column(name = "importance")
    private Importance importance;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private DepartmentEntity departmentEntity;

    @Column(name = "author")
    private String author;

    @Column(name = "signatory_person")
    private String signatoryPerson;

    @Column(name = "signatory_position", columnDefinition = "TEXT")
    private String signatoryPosition;

    @Column(name = "producer_person")
    private String producerPerson;

    @Column(name = "producer_position", columnDefinition = "TEXT")
    private String producerPosition;

    @Column(name = "producer_email")
    private String producerEmail;

    @Column(name = "producer_phone")
    private String producerPhone;

    @Column(name = "petrostat_request_number")
    private String petrostatNumber;

    @Column(name = "internal_number")
    private Integer internalNumber;

    @OneToMany(cascade = CascadeType.PERSIST, mappedBy = "oneTimeRequestEntity")
    private List<OneTimeRequestFileEntity> oneTimeRequestFileEntities;

    @ManyToOne
    @JoinColumn(name = "publication_status_id")
    private OneTimeRequestStatusEntity oneTimeRequestStatusEntity;
}