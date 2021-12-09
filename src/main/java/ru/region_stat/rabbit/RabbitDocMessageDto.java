package ru.region_stat.rabbit;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RabbitDocMessageDto {

    private String publicationId;

    private String fileId;

    private String name;

    private String fileName;

    private Boolean isArchive;

    private String documentText;
}