package ru.region_stat.domain.dto.statisticalPublication;

import lombok.*;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StatisticalPublicationCreateDto {

    private String name;

    private String complexName;

    private String publicationTypeEntityId;

    private List<String> rubricEntityId;

    private String formatEntityId;

    private Boolean isArchive;
}