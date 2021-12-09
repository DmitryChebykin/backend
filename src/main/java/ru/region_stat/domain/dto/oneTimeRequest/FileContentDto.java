package ru.region_stat.domain.dto.oneTimeRequest;

import lombok.*;
import org.springframework.core.io.ByteArrayResource;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileContentDto {
    ByteArrayResource byteArrayResource;

    String fileName;
}