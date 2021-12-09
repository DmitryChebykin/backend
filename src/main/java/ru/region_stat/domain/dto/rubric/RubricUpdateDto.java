package ru.region_stat.domain.dto.rubric;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RubricUpdateDto {

    private String name;

    private String archiveName;

    private String parentId;

    private Boolean isArchive;
}