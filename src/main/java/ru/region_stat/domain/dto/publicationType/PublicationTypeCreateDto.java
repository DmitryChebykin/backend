package ru.region_stat.domain.dto.publicationType;

import lombok.*;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PublicationTypeCreateDto {
    private String name;
    private String code;
    private String period;
    private String submissionTime;
    private List<String> rubricEntityId;
    private String formatEntityId;
    private String subscription;
}