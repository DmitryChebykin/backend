package ru.region_stat.controller.fileViewer.oneTimeRequestAnswer;

import lombok.*;
import ru.region_stat.domain.dto.oneTimeRequest.OneTimeRequestResultDto;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class OneTimeRequestAnswerPreviewDto {
    boolean isAnswerRecognized;

    private String filName;

    private String fileExtension;

    private OneTimeRequestResultDto oneTimeRequestResultDto;
}