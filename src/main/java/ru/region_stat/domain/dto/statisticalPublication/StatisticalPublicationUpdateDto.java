package ru.region_stat.domain.dto.statisticalPublication;

import lombok.*;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StatisticalPublicationUpdateDto {

    private Long number;

    private String name;

    private String code;

    private String period;

    private Boolean isArchive;

    private String publicationTypeEntityId;

    private List<String> rubricEntityId;

    private String formatEntityId;
}