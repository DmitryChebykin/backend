package ru.region_stat.domain.dto.oneTimeRequest;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OneTimeRequestExtendedResultDto {
    private String modifierFullName;
    private int year;
    private String importance;
    private String resolution;
    private String content;
    private String createdByUser;
    private String petrostatText;
    private String producerPhone;
    private String theme;
    private String id;
    private String producerPerson;
    private String createdTime;
    private String lastModifiedTime;
    private String signatoryPosition;
    private String departmentId;
    private String author;
    private String statusName;
    private String departmentName;
    private String producerEmail;
    private String creatorFullName;
    private String identificator;
    private String producerPosition;
    private String publicationStatusId;
    private String month;
    private String modifiedByUser;
    private String signatoryPerson;
    private String petrostatRequestNumber;
}