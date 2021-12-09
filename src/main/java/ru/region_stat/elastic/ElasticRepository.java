package ru.region_stat.elastic;

import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
@Profile("!import")
public interface ElasticRepository extends ElasticsearchRepository<ElasticPublicationEntity, String> {
    Optional<ElasticPublicationEntity> findByPublicationId(String id);
}