package ru.region_stat.domain.dto.statisticalPublication;

import lombok.*;
import ru.region_stat.domain.dto.BaseResultDto;
import ru.region_stat.domain.dto.file.PublicationFileResultDto;
import ru.region_stat.domain.dto.format.PublicationFormatResultDto;
import ru.region_stat.domain.dto.publicationType.PublicationTypeResultDto;
import ru.region_stat.domain.dto.rubric.RubricResultDto;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StatisticalPublicationResultDto extends BaseResultDto {

    private Long number;

    private String name;

    private String period;

    private Boolean isArchive;

    private List<PublicationFileResultDto> publicationFileEntities;

    private PublicationTypeResultDto publicationTypeEntity;

    private List<RubricResultDto> rubricEntityList;

    private PublicationFormatResultDto publicationFormatEntity;
}