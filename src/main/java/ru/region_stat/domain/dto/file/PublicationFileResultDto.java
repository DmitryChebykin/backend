package ru.region_stat.domain.dto.file;

import lombok.*;
import ru.region_stat.domain.dto.BaseResultDto;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PublicationFileResultDto extends BaseResultDto {
    private String fileName;
    private String fileExtension;
    private Long fileSize;
    private Boolean isArchive;
    private String publicationId;
    private Integer day;
    private Integer month;
    private Integer year;
}