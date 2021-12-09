package ru.region_stat.domain.dto.publicationSubscription;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PublicationSubscriptionUpdateDto {
    private String userEntityId;
    private String publicationTypeEntityId;
    private Boolean isEmailEnabled;
}