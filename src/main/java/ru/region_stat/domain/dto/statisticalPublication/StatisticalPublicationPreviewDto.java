package ru.region_stat.domain.dto.statisticalPublication;

import lombok.*;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StatisticalPublicationPreviewDto {
    private String name;

    private String fileName;

    private Long fileSize;

    private String publicationTypeEntityName;

    private List<String> rubricEntityNameList;

    private String publicationFormatEntityName;

    private String publicationTypeEntitySubscription;

    private Integer year;

    private Integer month;

    private String monthName;
}