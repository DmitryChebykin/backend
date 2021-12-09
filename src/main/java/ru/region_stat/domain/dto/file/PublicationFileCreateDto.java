package ru.region_stat.domain.dto.file;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PublicationFileCreateDto {
    private String fileName;
    private String fileExtension;
    private Long fileSize;
    private Boolean isArchive;
    private byte[] content;
    private String publicationId;
    private Integer day;
    private Integer month;
    private Integer year;
}