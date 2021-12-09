package ru.region_stat.domain.dto.oneTimeRequest;

import lombok.*;
import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OneTimeRequestFileResultDto {

    private UUID id;

    private String fileName;

    private String fileExtension;

    private Long fileSize;

    private UUID fileContentId;
}