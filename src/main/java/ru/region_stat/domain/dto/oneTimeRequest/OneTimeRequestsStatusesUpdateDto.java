package ru.region_stat.domain.dto.oneTimeRequest;

import lombok.*;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class OneTimeRequestsStatusesUpdateDto {
    private List<UUID> requestIds;
    private UUID statusId;
}