package ru.region_stat.aspects.rabbit;

import lombok.*;
import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RabbitDocMessageDto {
    private UUID id;

    private UUID fileId;

    private String name;

    private String fileName;

    private Boolean isArchive;

    private byte[] fileContent;
}