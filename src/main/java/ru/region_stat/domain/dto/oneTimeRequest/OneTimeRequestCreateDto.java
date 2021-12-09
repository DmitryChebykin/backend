package ru.region_stat.domain.dto.oneTimeRequest;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class OneTimeRequestCreateDto {

    private String theme;

    private String content;

    private String petrostatText;

    private Integer identificator;

    private String importanceName;

    private String departmentEntityId;

    private String signatoryPerson;

    private String signatoryPosition;

    private String producerPerson;

    private String producerPosition;

    private String producerEmail;

    private String producerPhone;

    private String petrostatNumber;

    private String author;

    private String oneTimeRequestStatusEntityId;

    private String resolution;
}