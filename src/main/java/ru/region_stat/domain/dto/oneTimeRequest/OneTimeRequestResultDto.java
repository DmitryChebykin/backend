package ru.region_stat.domain.dto.oneTimeRequest;

import lombok.*;
import ru.region_stat.domain.dto.BaseResultDto;
import ru.region_stat.domain.dto.department.DepartmentResultDto;
import ru.region_stat.domain.dto.oneTimeRequestStatus.OneTimeRequestStatusResultDto;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OneTimeRequestResultDto extends BaseResultDto {

    private String theme;

    private Integer identificator;

    private String content;

    private String petrostatText;

    private String resolution;

    private String importanceName;

    private DepartmentResultDto departmentEntity;

    private String author;

    private String signatoryPerson;

    private String signatoryPosition;

    private String producerPerson;

    private String producerPosition;

    private String producerEmail;

    private String producerPhone;

    private String petrostatNumber;

    private List<OneTimeRequestFileResultDto> oneTimeRequestFileEntityList;

    private OneTimeRequestStatusResultDto oneTimeRequestStatusEntity;
}