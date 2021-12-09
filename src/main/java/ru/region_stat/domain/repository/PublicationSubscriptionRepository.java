package ru.region_stat.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ru.region_stat.domain.entity.publicationSubscription.PublicationSubscriptionEntity;
import java.util.List;
import java.util.UUID;

@Repository
public interface PublicationSubscriptionRepository extends JpaRepository<PublicationSubscriptionEntity, UUID>, JpaSpecificationExecutor<PublicationSubscriptionEntity> {
    List<PublicationSubscriptionEntity> findAllByUserEntityId(UUID id);
}