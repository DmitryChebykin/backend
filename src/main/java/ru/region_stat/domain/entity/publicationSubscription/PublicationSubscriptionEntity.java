package ru.region_stat.domain.entity.publicationSubscription;

import lombok.*;
import ru.region_stat.domain.entity.BaseEntity;
import ru.region_stat.domain.entity.publicationType.PublicationTypeEntity;
import ru.region_stat.domain.entity.user.UserEntity;
import javax.persistence.*;

@Entity
@Table(name = "rs_publication_subscriptions",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "publication_type_id"})})
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PublicationSubscriptionEntity extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity userEntity;

    @ManyToOne
    @JoinColumn(name = "publication_type_id")
    private PublicationTypeEntity publicationTypeEntity;

    @Column(name = "is_email_enabled")
    private Boolean isEmailEnabled;
}