package ru.region_stat.elastic;

import lombok.*;
import ru.region_stat.domain.dto.file.PublicationFileExtendedResultDto;
import java.util.List;
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ElasticDocumentResultDto {

    private String publicationId;

    private String fileId;

    private String name;

    private List<String> highLight;

    private PublicationFileExtendedResultDto publicationFileExtendedResultDto;
}